package com.revline.service;

import com.revline.model.Car;
import com.revline.model.Status;
import com.revline.repository.CarRepository;
import com.revline.util.Validator;
import com.revline.util.exception.CarNotAvailableException;
import com.revline.util.exception.InvalidCarException;
import com.revline.util.exception.RepositoryException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Shared inventory validation, key construction, and read-only metric operations. */
final class InventorySupport {
    private InventorySupport() {}

    static void validateSpecs(Car car) {
        if (car == null) throw new InvalidCarException("Car cannot be null.");
        Validator.validateStringNoDelimiter(car.getBrand(), "Brand");
        Validator.validateStringNoDelimiter(car.getModel(), "Model");
        Validator.validateTopSpeed(car.getTopSpeed());
        Validator.validateHorsepower(car.getHorsepower());
        Validator.validateAcceleration(car.getAcceleration());
    }

    static String duplicateKey(String brand, String model) {
        return brand.trim().toLowerCase() + "|" + model.trim().toLowerCase();
    }

    static void replaceInList(List<Car> cars, Car replacement) {
        for (int i = 0; i < cars.size(); i++) {
            if (cars.get(i).getId() == replacement.getId()) {
                cars.set(i, replacement.copy());
                return;
            }
        }
        cars.add(replacement.copy());
    }

    static Set<String> getBrandsSorted(Collection<Car> cars) {
        Set<String> brands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Car car : cars) brands.add(car.getBrand());
        return brands;
    }

    static int countAvailable(Collection<Car> cars) {
        return (int) cars.stream().filter(car -> car.getStatus() == Status.AVAILABLE).count();
    }

    static double availableStockValue(Collection<Car> cars) {
        return cars.stream().filter(car -> car.getStatus() == Status.AVAILABLE).mapToDouble(Car::getPrice).sum();
    }

    static double portfolioValue(Collection<Car> cars) {
        return cars.stream().mapToDouble(Car::getPrice).sum();
    }

    static int resetStaleReservations(List<Car> cars, Map<Integer, Car> index, CarRepository repository)
            throws RepositoryException {
        int count = 0;
        for (Car c : cars) {
            if (c.getStatus() == Status.RESERVED) {
                c.setStatus(Status.AVAILABLE);
                Car inIndex = index.get(c.getId());
                if (inIndex != null) {
                    inIndex.setStatus(Status.AVAILABLE);
                }
                repository.save(c);
                count++;
            }
        }
        return count;
    }

    static void verifyUpdatePermitted(Car existing, Car updated, int id, Set<String> duplicateGuard)
            throws CarNotAvailableException, InvalidCarException {
        if (existing.getStatus() != Status.AVAILABLE) {
            throw new CarNotAvailableException(id,
                    "Cannot update car with status " + existing.getStatus() + "; vehicle must be AVAILABLE.");
        }
        String oldKey = duplicateKey(existing.getBrand(), existing.getModel());
        String newKey = duplicateKey(updated.getBrand(), updated.getModel());
        if (!oldKey.equals(newKey) && duplicateGuard.contains(newKey)) {
            throw new InvalidCarException("Another vehicle with brand '" + updated.getBrand()
                    + "' and model '" + updated.getModel() + "' already exists.");
        }
    }

    static void checkRestorePermitted(Car car, Car existing, Set<String> duplicateGuard) throws InvalidCarException {
        String restoredKey = duplicateKey(car.getBrand(), car.getModel());
        if (duplicateGuard.contains(restoredKey)
                && (existing == null || !duplicateKey(existing.getBrand(), existing.getModel()).equals(restoredKey))) {
            throw new InvalidCarException("Cannot restore car #" + car.getId()
                    + " because another car already uses brand/model '" + car.getBrand() + " " + car.getModel() + "'.");
        }
    }
}
