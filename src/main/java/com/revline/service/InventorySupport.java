package com.revline.service;

import com.revline.model.Car;
import com.revline.model.Status;
import com.revline.util.Validator;
import com.revline.util.exception.InvalidCarException;

import java.util.Collection;
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

    static void replaceInList(java.util.List<Car> cars, Car replacement) {
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
}
