package com.revline.service;

import com.revline.model.Car;

import java.util.Comparator;

/**
 * Library of vehicle comparators for custom showroom sorting (L8).
 */
public final class CarComparators {

    private CarComparators() {}

    /** Sorts by price ascending (lowest first). */
    public static final Comparator<Car> BY_PRICE_ASC = Comparator.comparingDouble(Car::getPrice);

    /** Sorts by price descending (highest first). */
    public static final Comparator<Car> BY_PRICE_DESC = BY_PRICE_ASC.reversed();

    /** Sorts by top speed descending (fastest first). */
    public static final Comparator<Car> BY_SPEED_DESC = (c1, c2) -> Integer.compare(c2.getTopSpeed(), c1.getTopSpeed());

    /** Sorts by engine horsepower descending (most powerful first). */
    public static final Comparator<Car> BY_HP_DESC = (c1, c2) -> Integer.compare(c2.getHorsepower(), c1.getHorsepower());

    /** Sorts by 0-100 acceleration time ascending (quickest first). */
    public static final Comparator<Car> BY_ACCEL_ASC = Comparator.comparingDouble(Car::getAcceleration);

    /** Sorts alphabetically by brand, then by model (L8 secondary tie-breaking). */
    public static final Comparator<Car> BY_BRAND_MODEL = Comparator
            .comparing(Car::getBrand, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Car::getModel, String.CASE_INSENSITIVE_ORDER);

    // =========================================================================
    // STUDENT EXERCISE EX-2 ZONE
    // =========================================================================

    /**
     * EX-2: Value-for-money comparator (L8).
     * Ranks cars by horsepower per $100,000 of price (higher is better).
     * Formula: score = (horsepower / price) * 100,000.0
     * Higher score comes first (descending).
     * If two cars have identical scores (within 0.001), tie-break by acceleration ascending (quicker 0-100 first).
     *
     * @return Comparator ranking cars by value-for-money
     * @see docs/learning/EX2_HINT.md
     */
    public static Comparator<Car> byValueForMoney() {
        return (c1, c2) -> {
            double score1 = ((double) c1.getHorsepower() / c1.getPrice()) * 100000.0;
            double score2 = ((double) c2.getHorsepower() / c2.getPrice()) * 100000.0;

            if (Math.abs(score1 - score2) > 0.001) {
                return Double.compare(score2, score1);
            }
            return Double.compare(c1.getAcceleration(), c2.getAcceleration());
        };
    }
}
