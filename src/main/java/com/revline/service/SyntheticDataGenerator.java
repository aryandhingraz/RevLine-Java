package com.revline.service;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for synthetic supercar datasets for benchmarking and stress testing (L4).
 */
final class SyntheticDataGenerator {

    private static final String[] SYNTHETIC_BRANDS = {
            "Apex", "Veloce", "Aero", "Hyperion", "Titan", "Phantom", "Stratos", "Zephyr"
    };

    private static final String[] SYNTHETIC_MODELS = {
            "GT-1", "Valkyrie-X", "Evo", "Spyder", "Prototipo", "Stradale", "Corsa", "Ultima"
    };

    private SyntheticDataGenerator() {}

    static List<Car> generate(int count) {
        if (count <= 0) return new ArrayList<>();

        List<Car> generated = new ArrayList<>(count);
        Random rng = new Random(42);

        for (int i = 1; i <= count; i++) {
            String brand = SYNTHETIC_BRANDS[rng.nextInt(SYNTHETIC_BRANDS.length)];
            String model = SYNTHETIC_MODELS[rng.nextInt(SYNTHETIC_MODELS.length)] + " [SYN-" + i + "]";
            double price = 100_000.0 + (rng.nextInt(290) * 10_000.0);
            int speed = 250 + rng.nextInt(200);
            FuelType fuel = FuelType.values()[rng.nextInt(FuelType.values().length)];
            int hp = 500 + rng.nextInt(1500);
            double accel = Math.round((1.8 + (rng.nextDouble() * 2.5)) * 100.0) / 100.0;

            generated.add(new Car(1000 + i, brand, model, price, speed, fuel, hp, accel, Status.AVAILABLE));
        }

        return generated;
    }
}
