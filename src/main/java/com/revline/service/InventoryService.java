package com.revline.service;

import com.revline.model.Car;
import com.revline.model.Status;
import com.revline.repository.CarRepository;
import com.revline.util.AppLogger;
import com.revline.util.Validator;
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
import java.util.TreeSet;

/**
 * Core inventory business service managing showroom vehicles.
 * Demonstrates Collections (ArrayList, HashMap, HashSet, TreeSet - L5, L6, L7)
 * and thread synchronization (L13, L14).
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

    /**
     * Reloads in-memory collections from the underlying repository.
     */
    public final void reload() throws RepositoryException {
        synchronized (lock) {
            cars.clear();
            index.clear();
            duplicateGuard.clear();

            List<Car> fromRepo = repository.findAll();
            for (Car c : fromRepo) {
                cars.add(c.copy());
                index.put(c.getId(), c.copy());
                duplicateGuard.add(makeDuplicateKey(c.getBrand(), c.getModel()));
            }
        }
    }

    /**
     * Adds a new car to inventory with duplicate prevention and ID assignment.
     */
    public Car addCar(Car car) throws RepositoryException {
        if (car == null) {
            throw new InvalidCarException("Car cannot be null.");
        }

        Validator.validateStringNoDelimiter(car.getBrand(), "Brand");
        Validator.validateStringNoDelimiter(car.getModel(), "Model");
        Validator.validateTopSpeed(car.getTopSpeed());
        Validator.validateHorsepower(car.getHorsepower());
        Validator.validateAcceleration(car.getAcceleration());

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
                undoManager.push(new UndoManager.UndoAction(
                        UndoManager.ActionType.ADD, toSave.copy(),
                        "Added Car #" + toSave.getId() + " (" + toSave.getBrand() + " " + toSave.getModel() + ")"));
            }

            if (logger != null) {
                logger.log("SELLER", "ADD_CAR", "id=" + toSave.getId() + " brand="
                        + toSave.getBrand() + " model=" + toSave.getModel());
            }

            return toSave.copy();
        }
    }

    /**
     * Updates an existing vehicle's specifications.
     * Only AVAILABLE vehicles can be edited.
     */
    public Car updateCar(int id, Car updated) throws RepositoryException, CarNotFoundException, CarNotAvailableException {
        if (updated == null) {
            throw new InvalidCarException("Updated car cannot be null.");
        }

        Validator.validateStringNoDelimiter(updated.getBrand(), "Brand");
        Validator.validateStringNoDelimiter(updated.getModel(), "Model");
        Validator.validateTopSpeed(updated.getTopSpeed());
        Validator.validateHorsepower(updated.getHorsepower());
        Validator.validateAcceleration(updated.getAcceleration());

        synchronized (lock) {
            Car existing = index.get(id);
            if (existing == null) {
                throw new CarNotFoundException(id);
            }
            if (existing.getStatus() != Status.AVAILABLE) {
                throw new CarNotAvailableException(id,
                        "Cannot update car with status " + existing.getStatus() + "; vehicle must be AVAILABLE.");
            }

            String oldKey = makeDuplicateKey(existing.getBrand(), existing.getModel());
            String newKey = makeDuplicateKey(updated.getBrand(), updated.getModel());
            if (!oldKey.equals(newKey) && duplicateGuard.contains(newKey)) {
                throw new InvalidCarException("Another vehicle with brand '" + updated.getBrand()
                        + "' and model '" + updated.getModel() + "' already exists.");
            }

            Car toSave = new Car(id, updated.getBrand(), updated.getModel(), updated.getPrice(),
                    updated.getTopSpeed(), updated.getFuelType(), updated.getHorsepower(),
                    updated.getAcceleration(), existing.getStatus());

            if (undoManager != null) {
                undoManager.push(new UndoManager.UndoAction(
                        UndoManager.ActionType.UPDATE, existing.copy(),
                        "Updated Car #" + id + " (" + existing.getBrand() + " " + existing.getModel() + ")"));
            }

            repository.save(toSave);

            for (int i = 0; i < cars.size(); i++) {
                if (cars.get(i).getId() == id) {
                    cars.set(i, toSave.copy());
                    break;
                }
            }
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

    /**
     * Deletes an unsold vehicle from inventory.
     * Only AVAILABLE vehicles can be deleted.
     */
    public Car deleteCar(int id) throws RepositoryException, CarNotFoundException, CarNotAvailableException {
        synchronized (lock) {
            Car existing = index.get(id);
            if (existing == null) {
                throw new CarNotFoundException(id);
            }
            if (existing.getStatus() != Status.AVAILABLE) {
                throw new CarNotAvailableException(id,
                        "Cannot delete car with status " + existing.getStatus() + "; vehicle must be AVAILABLE.");
            }

            if (undoManager != null) {
                undoManager.push(new UndoManager.UndoAction(
                        UndoManager.ActionType.DELETE, existing.copy(),
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

    /**
     * Restores a car into storage and in-memory indices without creating an undo action.
     * Invoked by UndoManager.
     */
    public void restoreCar(Car car) throws RepositoryException {
        if (car == null) return;
        synchronized (lock) {
            int id = car.getId();
            Car existing = index.get(id);
            if (existing != null) {
                duplicateGuard.remove(makeDuplicateKey(existing.getBrand(), existing.getModel()));
            }

            repository.save(car);

            boolean updated = false;
            for (int i = 0; i < cars.size(); i++) {
                if (cars.get(i).getId() == id) {
                    cars.set(i, car.copy());
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                cars.add(car.copy());
            }
            index.put(id, car.copy());
            duplicateGuard.add(makeDuplicateKey(car.getBrand(), car.getModel()));

            if (logger != null) {
                logger.log("SYSTEM", "RESTORE_CAR", "Restored Car #" + id + " via Undo");
            }
        }
    }

    /**
     * Reverts a newly added car from storage and in-memory indices without creating an undo action.
     * Invoked by UndoManager.
     */
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

    /**
     * Updates only the vehicle status (e.g. from AVAILABLE to RESERVED or SOLD).
     */
    public void updateCarStatus(int id, Status newStatus) throws RepositoryException, CarNotFoundException {
        synchronized (lock) {
            Car existing = index.get(id);
            if (existing == null) {
                throw new CarNotFoundException(id);
            }
            Car updated = new Car(existing.getId(), existing.getBrand(), existing.getModel(),
                    existing.getPrice(), existing.getTopSpeed(), existing.getFuelType(),
                    existing.getHorsepower(), existing.getAcceleration(), newStatus);

            repository.save(updated);

            for (int i = 0; i < cars.size(); i++) {
                if (cars.get(i).getId() == id) {
                    cars.set(i, updated.copy());
                    break;
                }
            }
            index.put(id, updated.copy());
        }
    }

    /**
     * Returns the intrinsic monitor lock object used by InventoryService.
     * Allows coordinated atomic locking with SalesService (MASTER_PROMPT line 665).
     */
    public Object getLock() {
        return lock;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * Returns an unmodifiable defensive copy of the current inventory list (L5).
     */
    public List<Car> findAll() {
        synchronized (lock) {
            List<Car> copy = new ArrayList<>(cars.size());
            for (Car c : cars) {
                copy.add(c.copy());
            }
            return Collections.unmodifiableList(copy);
        }
    }

    /**
     * Finds a car by its unique ID in O(1) time.
     *
     * @throws CarNotFoundException if no car exists with the given ID
     */
    public Car findById(int id) throws CarNotFoundException {
        synchronized (lock) {
            Car c = index.get(id);
            if (c == null) {
                throw new CarNotFoundException(id);
            }
            return c.copy();
        }
    }

    public boolean existsById(int id) {
        synchronized (lock) {
            return index.containsKey(id);
        }
    }

    /**
     * Returns a sorted set of all distinct brand names (L5 TreeSet).
     */
    public Set<String> getBrandsSorted() {
        synchronized (lock) {
            Set<String> brands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            for (Car c : cars) {
                brands.add(c.getBrand());
            }
            return brands;
        }
    }

    public int count() {
        synchronized (lock) {
            return cars.size();
        }
    }

    public int countAvailable() {
        synchronized (lock) {
            int available = 0;
            for (Car c : cars) {
                if (c.getStatus() == Status.AVAILABLE) {
                    available++;
                }
            }
            return available;
        }
    }

    /**
     * Computes the stock value over AVAILABLE cars only (fixes C prototype defect, ADR-009).
     */
    public double calculateTotalStockValue() {
        synchronized (lock) {
            double total = 0.0;
            for (Car c : cars) {
                if (c.getStatus() == Status.AVAILABLE) {
                    total += c.getPrice();
                }
            }
            return total;
        }
    }

    /**
     * Computes the total portfolio value across all cars including sold.
     */
    public double calculateTotalPortfolioValue() {
        synchronized (lock) {
            double total = 0.0;
            for (Car c : cars) {
                total += c.getPrice();
            }
            return total;
        }
    }

    private String makeDuplicateKey(String brand, String model) {
        return brand.trim().toLowerCase() + "|" + model.trim().toLowerCase();
    }
}
