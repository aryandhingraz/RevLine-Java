package com.revline.service;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fluent builder for composing complex vehicle filter predicates (L21 Functional Interfaces).
 */
public class FilterBuilder {

    private final List<Predicate<Car>> predicates = new ArrayList<>();

    public FilterBuilder() {}

    /** Matches brand case-insensitively. */
    public FilterBuilder brand(String brand) {
        if (brand != null && !brand.trim().isEmpty()) {
            String b = brand.trim();
            predicates.add(c -> c.getBrand().equalsIgnoreCase(b));
        }
        return this;
    }

    /** Matches specific fuel type. */
    public FilterBuilder fuelType(FuelType fuelType) {
        if (fuelType != null) {
            predicates.add(c -> c.getFuelType() == fuelType);
        }
        return this;
    }

    /** Matches minimum price. */
    public FilterBuilder minPrice(double min) {
        if (min > 0.0) {
            predicates.add(c -> c.getPrice() >= min);
        }
        return this;
    }

    /** Matches maximum budget. */
    public FilterBuilder maxPrice(double max) {
        if (max > 0.0) {
            predicates.add(c -> c.getPrice() <= max);
        }
        return this;
    }

    /** Matches minimum top speed in km/h. */
    public FilterBuilder minSpeed(int speed) {
        if (speed > 0) {
            predicates.add(c -> c.getTopSpeed() >= speed);
        }
        return this;
    }

    /** Matches minimum horsepower. */
    public FilterBuilder minHorsepower(int hp) {
        if (hp > 0) {
            predicates.add(c -> c.getHorsepower() >= hp);
        }
        return this;
    }

    /** Matches maximum 0-100 acceleration time. */
    public FilterBuilder maxAcceleration(double accel) {
        if (accel > 0.0) {
            predicates.add(c -> c.getAcceleration() <= accel);
        }
        return this;
    }

    /** Matches specific status. */
    public FilterBuilder status(Status status) {
        if (status != null) {
            predicates.add(c -> c.getStatus() == status);
        }
        return this;
    }

    /** Matches only AVAILABLE cars. */
    public FilterBuilder availableOnly() {
        return status(Status.AVAILABLE);
    }

    /** Matches cars that are NOT sold (AVAILABLE or RESERVED). */
    public FilterBuilder notSold() {
        predicates.add(c -> c.getStatus() != Status.SOLD);
        return this;
    }

    /**
     * Combines all configured criteria using logical AND.
     *
     * @return composite Predicate
     */
    public Predicate<Car> build() {
        if (predicates.isEmpty()) {
            return c -> true; // Match everything if no criteria set
        }
        Predicate<Car> combined = predicates.get(0);
        for (int i = 1; i < predicates.size(); i++) {
            combined = combined.and(predicates.get(i));
        }
        return combined;
    }

    /**
     * Filters a list of cars according to the built composite predicate.
     */
    public List<Car> filter(List<Car> cars) {
        Predicate<Car> p = build();
        List<Car> matches = new ArrayList<>();
        for (Car c : cars) {
            if (p.test(c)) {
                matches.add(c);
            }
        }
        return matches;
    }
}
