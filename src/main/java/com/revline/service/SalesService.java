package com.revline.service;

import com.revline.model.Car;
import com.revline.model.Reservation;
import com.revline.model.Sale;
import com.revline.model.Status;
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

    // Shared monitor lock with InventoryService (MASTER_PROMPT line 665)
    private final Object lock;

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
     * Executes an atomic car sale transaction.
     *
     * Rules:
     * - Car must exist in inventory.
     * - Car must not already be SOLD.
     * - If car is RESERVED, it can only be purchased by the reserving buyer (unless expired).
     * - On success: status moves to SOLD, Sale record persisted, active reservations removed.
     * - On persistence failure: status rolls back to prior state.
     *
     * @param carId identifier of vehicle to purchase
     * @param buyerName name of customer purchasing the vehicle
     * @param sellerName name of showroom sales representative
     * @return recorded Sale transaction
     */
    public Sale sellCar(int carId, String buyerName, String sellerName) throws RevLineException {
        Validator.requireNonBlank(buyerName, "Buyer Name");
        Validator.requireNonBlank(sellerName, "Seller Name");

        synchronized (lock) {
            Car car = inventoryService.findById(carId);

            // Clean any stale holds
            reservationManager.purgeExpired();

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

            Status previousStatus = car.getStatus();
            int nextSaleId = saleRepository.nextId();
            Sale sale = new Sale(nextSaleId, carId, buyerName.trim(), sellerName.trim(), LocalDate.now(), car.getPrice());

            // Atomic state transition with rollback guarantee
            try {
                inventoryService.updateCarStatus(carId, Status.SOLD);
                saleRepository.save(sale);
            } catch (RepositoryException e) {
                // Rollback in-memory status
                try {
                    inventoryService.updateCarStatus(carId, previousStatus);
                } catch (Exception ignored) {}
                throw new RepositoryException("Failed to persist sale transaction. Rollback applied.", e);
            }

            // Cleanup reservation & waitlist on sale completion
            reservationManager.remove(carId);
            waitlistService.clear(carId);

            if (logger != null) {
                logger.log("SALES", "SELL_CAR", String.format("Sale #%d: Car #%d (%s %s) sold to '%s' by '%s' for $%,.2f",
                        sale.getSaleId(), car.getId(), car.getBrand(), car.getModel(),
                        sale.getBuyerName(), sale.getSellerName(), sale.getPrice()));
            }

            return sale;
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
            Car car = inventoryService.findById(carId);

            reservationManager.purgeExpired();

            if (car.getStatus() == Status.SOLD) {
                throw new CarNotAvailableException(carId, "Cannot reserve Car #" + carId + " because it is already SOLD.");
            }
            if (car.getStatus() == Status.RESERVED) {
                Reservation existing = reservationManager.getReservation(carId);
                if (existing != null) {
                    throw new CarNotAvailableException(carId, "Car #" + carId + " is already reserved by " + existing.getBuyerName());
                }
            }

            inventoryService.updateCarStatus(carId, Status.RESERVED);
            Reservation res = reservationManager.reserve(carId, buyerName, durationMinutes);

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
            List<Integer> expiredIds = reservationManager.purgeExpired();
            for (int id : expiredIds) {
                try {
                    Car car = inventoryService.findById(id);
                    if (car.getStatus() == Status.RESERVED) {
                        inventoryService.updateCarStatus(id, Status.AVAILABLE);
                        if (logger != null) {
                            logger.log("SYSTEM", "EXPIRE_HOLD", "Reservation expired for Car #" + id + "; returned to AVAILABLE");
                        }
                    }
                } catch (Exception ignored) {}
            }
            return expiredIds;
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
        if (sale == null) return "";
        String brandModel = (car != null) ? (car.getBrand() + " " + car.getModel()) : ("Car #" + sale.getCarId());
        String engineInfo = (car != null) ? (car.getHorsepower() + " HP | " + car.getTopSpeed() + " km/h") : "N/A";
        String fuelInfo = (car != null) ? car.getFuelType().name() : "N/A";

        StringBuilder sb = new StringBuilder();
        sb.append("=======================================================").append(System.lineSeparator());
        sb.append("                   REVLINE SHOWROOM                    ").append(System.lineSeparator());
        sb.append("                OFFICIAL SALES RECEIPT                 ").append(System.lineSeparator());
        sb.append("=======================================================").append(System.lineSeparator());
        sb.append(String.format("  Receipt No   : REC-%04d-%04d%n", sale.getSaleDate().getYear(), sale.getSaleId()));
        sb.append(String.format("  Sale ID      : #%d%n", sale.getSaleId()));
        sb.append(String.format("  Date         : %s%n", sale.getSaleDate()));
        sb.append(String.format("  Buyer        : %s%n", sale.getBuyerName()));
        sb.append(String.format("  Seller       : %s%n", sale.getSellerName()));
        sb.append("-------------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("  Vehicle ID   : #%d%n", sale.getCarId()));
        sb.append(String.format("  Vehicle      : %s%n", brandModel));
        sb.append(String.format("  Specs        : %s%n", engineInfo));
        sb.append(String.format("  Fuel Type    : %s%n", fuelInfo));
        sb.append("-------------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("  Selling Price: $%,.2f%n", sale.getPrice()));
        sb.append("  Taxes & Fees : INCLUDED").append(System.lineSeparator());
        sb.append(String.format("  TOTAL PAID   : $%,.2f%n", sale.getPrice()));
        sb.append("=======================================================").append(System.lineSeparator());
        sb.append("            THANK YOU FOR CHOOSING REVLINE!            ").append(System.lineSeparator());
        sb.append("=======================================================").append(System.lineSeparator());
        return sb.toString();
    }
}
