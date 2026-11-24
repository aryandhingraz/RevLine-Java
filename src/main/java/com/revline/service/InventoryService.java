package com.revline.service;

import com.revline.model.Car;
import com.revline.model.Status;
import com.revline.repository.CarRepository;
import com.revline.repository.FileCarRepository;
import com.revline.util.AppLogger;
import com.revline.util.exception.CarNotAvailableException;
import com.revline.util.exception.CarNotFoundException;
import com.revline.util.exception.InvalidCarException;
import com.revline.util.exception.RepositoryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Core inventory business service managing showroom vehicles (L5–L7, L13–L14).
 * Combines ArrayList, HashMap, and HashSet collections with intrinsic monitor locking.
 */
public class InventoryService {

    private final CarRepository repository;
    private final UndoManager undoManager;
    private final AppLogger logger;

    // Dual collection architecture (PPT Slide 5)
    private final List<Car> cars = new ArrayList<>();
    private final Map<Integer, Car> index = new HashMap<>();
    private final Set<String> duplicateGuard = new HashSet<>();
    private final Object lock = new Object();

    public InventoryService(CarRepository repository, AppLogger logger) throws RepositoryException {
        this(repository, new UndoManager(), logger);
    }

    public InventoryService(CarRepository repository, UndoManager undoManager, AppLogger logger) throws RepositoryException {
        this.repository = repository;
        this.undoManager = (undoManager != null) ? undoManager : new UndoManager();
        this.logger = logger;
        reload();
    }

    /** Reloads in-memory collections from underlying storage. */
    public final void reload() throws RepositoryException {
        synchronized (lock) {
            if (repository instanceof FileCarRepository) {
                ((FileCarRepository) repository).load();
            }
            cars.clear();
            index.clear();
            duplicateGuard.clear();
            for (Car c : repository.findAll()) {
                cars.add(c.copy());
                index.put(c.getId(), c.copy());
                duplicateGuard.add(InventorySupport.duplicateKey(c.getBrand(), c.getModel()));
            }
        }
    }

    /**
     * Resets any stale RESERVED vehicles to AVAILABLE on application startup.
     * Since in-memory reservations do not survive restarts, any vehicle left in
     * RESERVED status would otherwise be permanently stuck.
     *
     * @return the number of vehicles reset to AVAILABLE
     * @throws RepositoryException if saving fails
     */
    public int resetStaleReservations() throws RepositoryException {
        synchronized (lock) {
            int count = InventorySupport.resetStaleReservations(cars, index, repository);
            if (count > 0 && logger != null) {
                logger.log("SYSTEM", "RESERVATION_RESET",
                        "Reset " + count + " stale RESERVED vehicle(s) to AVAILABLE on startup.");
            }
            return count;
        }
    }

    /** Adds a new vehicle with duplicate checking and auto ID generation. */
    public Car addCar(Car car) throws RepositoryException {
        InventorySupport.validateSpecs(car);
        synchronized (lock) {
            String dupKey = InventorySupport.duplicateKey(car.getBrand(), car.getModel());
            if (duplicateGuard.contains(dupKey)) {
                throw new InvalidCarException("A vehicle with brand '" + car.getBrand()
                        + "' and model '" + car.getModel() + "' already exists.");
            }
            int id = car.getId();
            if (id <= 0 || index.containsKey(id)) {
                id = repository.nextId();
            }
            Car toSave = new Car(id, car.getBrand(), car.getModel(), car.getPrice(),
                    car.getTopSpeed(), car.getFuelType(), car.getHorsepower(),
                    car.getAcceleration(), car.getStatus());
            repository.save(toSave);
            cars.add(toSave.copy());
            index.put(toSave.getId(), toSave.copy());
            duplicateGuard.add(dupKey);
            if (undoManager != null) {
                undoManager.push(new UndoManager.UndoAction(UndoManager.ActionType.ADD, toSave.copy(),
                        "Added Car #" + toSave.getId() + " (" + toSave.getBrand() + " " + toSave.getModel() + ")"));
            }
            if (logger != null) {
                logger.log("SELLER", "ADD_CAR", "id=" + toSave.getId() + " brand=" + toSave.getBrand() + " model=" + toSave.getModel());
            }
            return toSave.copy();
        }
    }

    /** Updates an existing vehicle's specifications. Only AVAILABLE vehicles can be edited. */
    public Car updateCar(int id, Car updated) throws RepositoryException, CarNotFoundException, CarNotAvailableException {
        InventorySupport.validateSpecs(updated);
        synchronized (lock) {
            Car existing = index.get(id);
            if (existing == null) throw new CarNotFoundException(id);
            InventorySupport.verifyUpdatePermitted(existing, updated, id, duplicateGuard);

            String oldKey = InventorySupport.duplicateKey(existing.getBrand(), existing.getModel());
            String newKey = InventorySupport.duplicateKey(updated.getBrand(), updated.getModel());

            Car toSave = new Car(id, updated.getBrand(), updated.getModel(), updated.getPrice(),
                    updated.getTopSpeed(), updated.getFuelType(), updated.getHorsepower(),
                    updated.getAcceleration(), existing.getStatus());
            repository.save(toSave);
            if (undoManager != null) {
                undoManager.push(new UndoManager.UndoAction(UndoManager.ActionType.UPDATE, existing.copy(),
                        "Updated Car #" + id + " (" + existing.getBrand() + " " + existing.getModel() + ")"));
            }
            InventorySupport.replaceInList(cars, toSave);
            index.put(id, toSave.copy());
            if (!oldKey.equals(newKey)) {
                duplicateGuard.remove(oldKey);
                duplicateGuard.add(newKey);
            }
            if (logger != null) {
                logger.log("SELLER", "UPDATE_CAR", "id=" + id + " (" + toSave.getBrand() + " " + toSave.getModel() + ")");
            }
            return toSave.copy();
        }
    }

    /** Deletes an unsold vehicle from inventory. Only AVAILABLE vehicles can be deleted. */
    public Car deleteCar(int id) throws RepositoryException, CarNotFoundException, CarNotAvailableException {
        synchronized (lock) {
            Car existing = index.get(id);
            if (existing == null) throw new CarNotFoundException(id);
            if (existing.getStatus() != Status.AVAILABLE) {
                throw new CarNotAvailableException(id, "Cannot delete car with status " + existing.getStatus() + "; vehicle must be AVAILABLE.");
            }
            repository.deleteById(id);
            if (undoManager != null) {
                undoManager.push(new UndoManager.UndoAction(UndoManager.ActionType.DELETE, existing.copy(),
                        "Deleted Car #" + id + " (" + existing.getBrand() + " " + existing.getModel() + ")"));
            }
            cars.removeIf(c -> c.getId() == id);
            index.remove(id);
            duplicateGuard.remove(InventorySupport.duplicateKey(existing.getBrand(), existing.getModel()));
            if (logger != null) {
                logger.log("SELLER", "DELETE_CAR", "id=" + id + " (" + existing.getBrand() + " " + existing.getModel() + ")");
            }
            return existing.copy();
        }
    }

    /** Restores a car into storage and in-memory indices without creating an undo action (invoked by UndoManager). */
    public void restoreCar(Car car) throws RepositoryException {
        if (car == null) return;
        synchronized (lock) {
            int id = car.getId();
            Car existing = index.get(id);
            InventorySupport.checkRestorePermitted(car, existing, duplicateGuard);
            repository.save(car);
            if (existing != null) {
                duplicateGuard.remove(InventorySupport.duplicateKey(existing.getBrand(), existing.getModel()));
            }
            InventorySupport.replaceInList(cars, car);
            index.put(id, car.copy());
            duplicateGuard.add(InventorySupport.duplicateKey(car.getBrand(), car.getModel()));
            if (logger != null) {
                logger.log("SYSTEM", "RESTORE_CAR", "Restored Car #" + id + " via Undo");
            }
        }
    }

    /** Reverts an added car from storage and in-memory indices without an undo action (invoked by UndoManager). */
    public void removeCarInternal(int id) throws RepositoryException {
        synchronized (lock) {
            Car existing = index.get(id);
            if (existing == null) {
                throw new RepositoryException("Cannot undo addition: car #" + id + " no longer exists.");
            }
            repository.deleteById(id);
            cars.removeIf(c -> c.getId() == id);
            index.remove(id);
            duplicateGuard.remove(InventorySupport.duplicateKey(existing.getBrand(), existing.getModel()));
            if (logger != null) {
                logger.log("SYSTEM", "REMOVE_CAR_UNDO", "Removed Car #" + id + " via Undo");
            }
        }
    }

    /** Updates vehicle status (e.g. from AVAILABLE to RESERVED or SOLD). */
    public void updateCarStatus(int id, Status newStatus)
            throws RepositoryException, CarNotFoundException, CarNotAvailableException {
        synchronized (lock) {
            Car existing = index.get(id);
            if (existing == null) throw new CarNotFoundException(id);
            if (existing.getStatus() == Status.SOLD && newStatus != Status.SOLD) {
                throw new CarNotAvailableException(id,
                        "Vehicle is SOLD; terminal state transition to " + newStatus + " is impossible.");
            }
            applyCarStatus(existing, newStatus);
        }
    }

    /** Reverts vehicle status during transactional rollback (bypasses terminal SOLD check). */
    public void revertCarStatus(int id, Status targetStatus)
            throws RepositoryException, CarNotFoundException {
        synchronized (lock) {
            Car existing = index.get(id);
            if (existing == null) throw new CarNotFoundException(id);
            applyCarStatus(existing, targetStatus);
        }
    }

    private void applyCarStatus(Car existing, Status status) throws RepositoryException {
        Car updated = new Car(existing.getId(), existing.getBrand(), existing.getModel(),
                existing.getPrice(), existing.getTopSpeed(), existing.getFuelType(),
                existing.getHorsepower(), existing.getAcceleration(), status);
        repository.save(updated);
        InventorySupport.replaceInList(cars, updated);
        index.put(existing.getId(), updated.copy());
    }
    /** Returns the intrinsic monitor lock for coordinated locking with SalesService (L13). */
    public Object getLock() { return lock; }

    public UndoManager getUndoManager() { return undoManager; }
    /** Returns an unmodifiable defensive copy of the current inventory list (L5). */
    public List<Car> findAll() {
        synchronized (lock) {
            List<Car> copy = new ArrayList<>(cars.size());
            for (Car c : cars) copy.add(c.copy());
            return Collections.unmodifiableList(copy);
        }
    }
    public Car findById(int id) throws CarNotFoundException {
        synchronized (lock) {
            Car c = index.get(id);
            if (c == null) throw new CarNotFoundException(id);
            return c.copy();
        }
    }

    public boolean existsById(int id) {
        synchronized (lock) { return index.containsKey(id); }
    }

    public Set<String> getBrandsSorted() {
        synchronized (lock) { return InventorySupport.getBrandsSorted(cars); }
    }

    public int count() {
        synchronized (lock) { return cars.size(); }
    }

    public void saveSnapshot() throws RepositoryException {
        synchronized (lock) {
            repository.saveAll(findAll());
            if (logger != null) {
                logger.log("SYSTEM", "SNAPSHOT_SAVED", "Inventory snapshot saved (" + cars.size() + " vehicles)");
            }
        }
    }

    public int countAvailable() {
        synchronized (lock) { return InventorySupport.countAvailable(cars); }
    }

    public double calculateTotalStockValue() {
        synchronized (lock) { return InventorySupport.availableStockValue(cars); }
    }

    public double calculateTotalPortfolioValue() {
        synchronized (lock) { return InventorySupport.portfolioValue(cars); }
    }
}
