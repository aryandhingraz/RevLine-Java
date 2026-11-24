package com.revline.service;

import com.revline.model.Car;
import com.revline.model.Reservation;
import com.revline.model.Sale;
import com.revline.model.Status;
import com.revline.repository.FileSaleRepository;
import com.revline.repository.SaleRepository;
import com.revline.util.AppLogger;
import com.revline.util.Validator;
import com.revline.util.exception.CarNotAvailableException;
import com.revline.util.exception.CarNotFoundException;
import com.revline.util.exception.InvalidCarException;
import com.revline.util.exception.RepositoryException;
import com.revline.util.exception.RevLineException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Orchestrates showroom sales transactions and reservation lifecycles.
 * Demonstrates thread synchronization using a shared intrinsic monitor (L13, L14).
 */
public class SalesService {

    private final InventoryService inventoryService;
    private final SaleRepository saleRepository;
    private final ReservationManager reservationManager;
    private final WaitlistService waitlistService;
    private final AppLogger logger;

    // Shared monitor lock with InventoryService for cross-service thread coordination (L13, L14)
    private final Object lock;
    private final Set<Integer> pendingExpiredIds = new HashSet<>();

    public SalesService(InventoryService inventoryService,
                        SaleRepository saleRepository,
                        ReservationManager reservationManager,
                        WaitlistService waitlistService,
                        AppLogger logger) {
        if (inventoryService == null) throw new IllegalArgumentException("InventoryService cannot be null.");
        if (saleRepository == null) throw new IllegalArgumentException("SaleRepository cannot be null.");
        if (reservationManager == null) throw new IllegalArgumentException("ReservationManager cannot be null.");
        if (waitlistService == null) throw new IllegalArgumentException("WaitlistService cannot be null.");

        this.inventoryService = inventoryService;
        this.saleRepository = saleRepository;
        this.reservationManager = reservationManager;
        this.waitlistService = waitlistService;
        this.logger = logger;
        this.lock = inventoryService.getLock();
    }

    /**
     * Executes a vehicle sale transaction following strict consistency rules (L13, L14, L19, L20):
     * Step 1: Validate input parameters
     * Step 2: Find vehicle and purge expired reservations
     * Step 3: Verify car availability (must not be sold; if reserved, buyer must match)
     * Step 4: Persist sale to repository (transaction in JDBC, safe write in CSV)
     * Step 5: Update in-memory status with rollback protection on storage failure
     * Step 6: Clean up active reservation and waitlist entries
     * Step 7: Record audit event in logger
     *
     * @param carId identifier of vehicle to purchase
     * @param buyerName name of customer purchasing the vehicle
     * @param sellerName name of showroom sales representative
     * @return recorded Sale transaction
     */
    public Sale sellCar(int carId, String buyerName, String sellerName) throws RevLineException {
        // Step 1: Validate input parameters
        Validator.requireNonBlank(buyerName, "Buyer Name");
        Validator.requireNonBlank(sellerName, "Seller Name");

        synchronized (lock) {
            // Step 2: Release expired holds and restore their vehicle states.
            purgeExpiredReservations();
            Car car = inventoryService.findById(carId);

            // Step 3: Verify state and reservation ownership
            validateCarAvailability(car, carId, buyerName);

            Status previousStatus = car.getStatus();
            int nextSaleId = saleRepository.nextId();
            Sale sale = new Sale(nextSaleId, carId, buyerName.trim(), sellerName.trim(), LocalDate.now(), car.getPrice());

            // Step 4 & 5: Persist sale with rollback protection
            persistSaleWithRollback(sale, car, previousStatus);

            // Step 6: Clean up reservation and waitlist
            reservationManager.remove(carId);
            waitlistService.clear(carId);

            // Step 7: Record audit log
            logSale(sale, car);

            return sale;
        }
    }

    private void validateCarAvailability(Car car, int carId, String buyerName) throws CarNotAvailableException {
        if (car.getStatus() == Status.SOLD) {
            throw new CarNotAvailableException(carId, "Car #" + carId + " is already SOLD.");
        }

        if (car.getStatus() == Status.RESERVED) {
            Reservation activeRes = reservationManager.getReservation(carId);
            if (activeRes != null && !activeRes.getBuyerName().equalsIgnoreCase(buyerName.trim())) {
                throw new CarNotAvailableException(carId,
                        "Car #" + carId + " is currently RESERVED by " + activeRes.getBuyerName()
                                + " until " + activeRes.getExpiresAt());
            }
        }
    }

    private void persistSaleWithRollback(Sale sale, Car car, Status previousStatus)
            throws RepositoryException, CarNotFoundException, CarNotAvailableException {
        int carId = car.getId();
        try {
            inventoryService.updateCarStatus(carId, Status.SOLD);
            saleRepository.recordSale(sale, car);
        } catch (RepositoryException e) {
            try {
                inventoryService.revertCarStatus(carId, previousStatus);
            } catch (RepositoryException | CarNotFoundException rollbackEx) {
                if (logger != null) {
                    logger.log("SALES", "ROLLBACK_FAILED",
                            "Failed to revert car #" + carId + " status: " + rollbackEx.getMessage());
                }
                e.addSuppressed(rollbackEx);
            }
            throw new RepositoryException("Failed to persist sale transaction. Rollback applied.", e);
        }
    }

    private void logSale(Sale sale, Car car) {
        if (logger != null) {
            logger.log("SALES", "SELL_CAR", String.format("Sale #%d: Car #%d (%s %s) sold to '%s' by '%s' for $%,.2f",
                    sale.getSaleId(), car.getId(), car.getBrand(), car.getModel(),
                    sale.getBuyerName(), sale.getSellerName(), sale.getPrice()));
        }
    }

    /**
     * Reserves a vehicle for a designated buyer for a fixed duration in minutes.
     *
     * @param carId target car ID
     * @param buyerName buyer placing the reservation
     * @param durationMinutes reservation hold duration in minutes
     * @return established Reservation
     */
    public Reservation reserveCar(int carId, String buyerName, int durationMinutes) throws RevLineException {
        Validator.requireNonBlank(buyerName, "Buyer Name");

        synchronized (lock) {
            // Expired reservations must also restore the persisted vehicle status.
            purgeExpiredReservations();
            Car car = inventoryService.findById(carId);

            if (car.getStatus() == Status.SOLD) {
                throw new CarNotAvailableException(carId, "Cannot reserve Car #" + carId + " because it is already SOLD.");
            }
            if (car.getStatus() == Status.RESERVED) {
                Reservation existing = reservationManager.getReservation(carId);
                String msg = existing != null
                        ? "Car #" + carId + " is already reserved by " + existing.getBuyerName()
                        : "Car #" + carId + " is already reserved.";
                throw new CarNotAvailableException(carId, msg);
            }

            // Create the in-memory hold first so invalid duration/details cannot
            // leave a vehicle marked RESERVED without a matching reservation.
            Reservation res = reservationManager.reserve(carId, buyerName, durationMinutes);
            try {
                inventoryService.updateCarStatus(carId, Status.RESERVED);
            } catch (RepositoryException | CarNotFoundException | CarNotAvailableException e) {
                reservationManager.remove(carId);
                throw e;
            }

            if (logger != null) {
                logger.log("SALES", "RESERVE_CAR", String.format("Car #%d reserved for '%s' for %d min",
                        carId, buyerName, durationMinutes));
            }

            return res;
        }
    }

    /**
     * Cancels an active reservation, returning the vehicle to AVAILABLE.
     */
    public void cancelReservation(int carId, String buyerName) throws RevLineException {
        synchronized (lock) {
            purgeExpiredReservations();
            Car car = inventoryService.findById(carId);

            reservationManager.cancel(carId, buyerName);

            // Revert vehicle to AVAILABLE if not sold
            if (car.getStatus() == Status.RESERVED) {
                inventoryService.updateCarStatus(carId, Status.AVAILABLE);
            }

            if (logger != null) {
                logger.log("SALES", "CANCEL_RESERVE", String.format("Reservation on Car #%d cancelled for '%s'",
                        carId, buyerName));
            }
        }
    }

    /**
     * Scans for expired reservations, restoring vehicles to AVAILABLE.
     */
    public List<Integer> purgeExpiredReservations() {
        synchronized (lock) {
            List<Integer> newlyExpiredIds = reservationManager.purgeExpired();
            pendingExpiredIds.addAll(newlyExpiredIds);

            Iterator<Integer> iterator = pendingExpiredIds.iterator();
            while (iterator.hasNext()) {
                int id = iterator.next();
                try {
                    Car car = inventoryService.findById(id);
                    if (car.getStatus() == Status.RESERVED) {
                        inventoryService.updateCarStatus(id, Status.AVAILABLE);
                        if (logger != null) {
                            logger.log("SYSTEM", "EXPIRE_HOLD",
                                    "Reservation expired for Car #" + id + "; returned to AVAILABLE");
                        }
                    }
                    iterator.remove();
                } catch (CarNotFoundException | CarNotAvailableException e) {
                    if (logger != null) {
                        logger.log("SYSTEM", "EXPIRE_HOLD_WARN",
                                "Car #" + id + " unavailable during expiry cleanup: " + e.getMessage());
                    }
                    iterator.remove();
                } catch (RepositoryException e) {
                    // Keep the ID pending so a later cleanup cycle can retry the persisted status update.
                    if (logger != null) {
                        logger.log("SYSTEM", "EXPIRE_HOLD_ERR",
                                "Failed to update car #" + id + "; will retry: " + e.getMessage());
                    }
                }
            }
            return newlyExpiredIds;
        }
    }

    public List<Sale> getSalesHistory() throws RepositoryException {
        return saleRepository.findAll();
    }

    public Sale getSaleById(int saleId) throws RepositoryException {
        return saleRepository.findById(saleId);
    }

    public List<Sale> getSalesByCarId(int carId) throws RepositoryException {
        return saleRepository.findByCarId(carId);
    }

    /**
     * Reloads sales data from storage if using file-backed repository.
     */
    public void reload() throws RepositoryException {
        if (saleRepository instanceof FileSaleRepository) {
            ((FileSaleRepository) saleRepository).load();
        }
    }

    public ReservationManager getReservationManager() {
        return reservationManager;
    }

    public WaitlistService getWaitlistService() {
        return waitlistService;
    }

    /**
     * Generates a formal ASCII receipt for a completed sale transaction.
     */
    public String generateReceipt(Sale sale, Car car) {
        return SalesReceiptFormatter.format(sale, car);
    }
}
