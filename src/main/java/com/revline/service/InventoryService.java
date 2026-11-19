package com.revline.service;

import com.revline.model.Car;
import com.revline.model.Status;
import com.revline.repository.CarRepository;
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
            cars.clear();
            index.clear();
            duplicateGuard.clear();
            for (Car c : repository.findAll()) {
                cars.add(c.copy());
                index.put(c.getId(), c.copy());
                duplicateGuard.add(makeDuplicateKey(c.getBrand(), c.getModel()));
            }
        }
    }

    private void validateSpecs(Car car) {
        InventorySupport.validateSpecs(car);
    }

    private void replaceInList(Car car) {
        InventorySupport.replaceInList(cars, car);
    }

    /** Adds a new vehicle with duplicate checking and auto ID generation. */
    public Car addCar(Car car) throws RepositoryException {
        validateSpecs(car);
        synchronized (lock) {
            String dupKey = makeDuplicateKey(car.getBrand(), car.getModel());
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
        validateSpecs(updated);
        synchronized (lock) {
            Car existing = index.get(id);
            if (existing == null) throw new CarNotFoundException(id);
            if (existing.getStatus() != Status.AVAILABLE) {
                throw new CarNotAvailableException(id, "Cannot update car with status " + existing.getStatus() + "; vehicle must be AVAILABLE.");
            }
            String oldKey = makeDuplicateKey(existing.getBrand(), existing.getModel());
            String newKey = makeDuplicateKey(updated.getBrand(), updated.getModel());
            if (!oldKey.equals(newKey) && duplicateGuard.contains(newKey)) {
                throw new InvalidCarException("Another vehicle with brand '" + updated.getBrand() + "' and model '" + updated.getModel() + "' already exists.");
            }
            Car toSave = new Car(id, updated.getBrand(), updated.getModel(), updated.getPrice(),
                    updated.getTopSpeed(), updated.getFuelType(), updated.getHorsepower(),
                    updated.getAcceleration(), existing.getStatus());
            if (undoManager != null) {
                undoManager.push(new UndoManager.UndoAction(UndoManager.ActionType.UPDATE, existing.copy(),
                        "Updated Car #" + id + " (" + existing.getBrand() + " " + existing.getModel() + ")"));
            }
            repository.save(toSave);
            replaceInList(toSave);
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
            if (undoManager != null) {
                undoManager.push(new UndoManager.UndoAction(UndoManager.ActionType.DELETE, existing.copy(),
                        "Deleted Car #" + id + " (" + existing.getBrand() + " " + existing.getModel() + ")"));
            }
            repository.deleteById(id);
            cars.removeIf(c -> c.getId() == id);
            index.remove(id);
            duplicateGuard.remove(makeDuplicateKey(existing.getBrand(), existing.getModel()));
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
            if (existing != null) {
                duplicateGuard.remove(makeDuplicateKey(existing.getBrand(), existing.getModel()));
            }
            repository.save(car);
            replaceInList(car);
            index.put(id, car.copy());
            duplicateGuard.add(makeDuplicateKey(car.getBrand(), car.getModel()));
            if (logger != null) {
                logger.log("SYSTEM", "RESTORE_CAR", "Restored Car #" + id + " via Undo");
            }
        }
    }

    /** Reverts an added car from storage and in-memory indices without an undo action (invoked by UndoManager). */
    public void removeCarInternal(int id) throws RepositoryException {
        synchronized (lock) {
            Car existing = index.get(id);
            if (existing != null) {
                repository.deleteById(id);
                cars.removeIf(c -> c.getId() == id);
                index.remove(id);
                duplicateGuard.remove(makeDuplicateKey(existing.getBrand(), existing.getModel()));
                if (logger != null) {
                    logger.log("SYSTEM", "REMOVE_CAR_UNDO", "Removed Car #" + id + " via Undo");
                }
            }
        }
    }

    /** Updates vehicle status (e.g. from AVAILABLE to RESERVED or SOLD). */
    public void updateCarStatus(int id, Status newStatus) throws RepositoryException, CarNotFoundException {
        synchronized (lock) {
            Car existing = index.get(id);
            if (existing == null) throw new CarNotFoundException(id);
            Car updated = new Car(existing.getId(), existing.getBrand(), existing.getModel(),
                    existing.getPrice(), existing.getTopSpeed(), existing.getFuelType(),
                    existing.getHorsepower(), existing.getAcceleration(), newStatus);
            repository.save(updated);
            replaceInList(updated);
            index.put(id, updated.copy());
        }
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
    /** Finds a car by ID in O(1) time. */
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

    /** Returns a sorted set of distinct brand names (L5 TreeSet). */
    public Set<String> getBrandsSorted() {
        synchronized (lock) {
            return InventorySupport.getBrandsSorted(cars);
        }
    }

    public int count() {
        synchronized (lock) { return cars.size(); }
    }

    /** Persists a point-in-time snapshot of the inventory to underlying storage. */
    public void saveSnapshot() throws RepositoryException {
        synchronized (lock) {
            repository.saveAll(findAll());
            if (logger != null) {
                logger.log("SYSTEM", "SNAPSHOT_SAVED", "Inventory snapshot saved (" + cars.size() + " vehicles)");
            }
        }
    }

    public int countAvailable() {
        synchronized (lock) {
            return InventorySupport.countAvailable(cars);
        }
    }

    /** Computes stock value over AVAILABLE cars only (fixes C prototype defect, ADR-009). */
    public double calculateTotalStockValue() {
        synchronized (lock) {
            return InventorySupport.availableStockValue(cars);
        }
    }

    /** Computes total portfolio value across all cars including sold. */
    public double calculateTotalPortfolioValue() {
        synchronized (lock) {
            return InventorySupport.portfolioValue(cars);
        }
    }

    private String makeDuplicateKey(String brand, String model) {
        return InventorySupport.duplicateKey(brand, model);
    }
}
