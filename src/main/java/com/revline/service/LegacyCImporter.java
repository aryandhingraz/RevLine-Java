package com.revline.service;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.util.AppLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Reads the original comma-delimited C inventory format. */
final class LegacyCImporter {
    private LegacyCImporter() {}

    static DataToolsService.LegacyImportResult importFile(File file, InventoryService inventory,
                                                           AppLogger logger) throws IOException {
        if (file == null || !file.exists()) throw new IllegalArgumentException("Legacy file does not exist: " + file);
        int imported = 0;
        int skipped = 0;
        List<Car> cars = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                String[] fields = trimmed.split(",", -1);
                if (fields.length < 9) { skipped++; continue; }
                try {
                    Car car = parse(fields);
                    if (inventory != null) inventory.addCar(car);
                    cars.add(car);
                    imported++;
                } catch (Exception e) {
                    skipped++;
                }
            }
        }
        if (logger != null) logger.log("DATA_TOOLS", "LEGACY_IMPORT",
                String.format("Imported %d cars, skipped %d from %s", imported, skipped, file.getName()));
        return new DataToolsService.LegacyImportResult(imported, skipped, cars);
    }

    private static Car parse(String[] fields) {
        int id = Integer.parseInt(fields[0].trim());
        String brand = fields[1].trim();
        String model = fields[2].trim();
        double price = Double.parseDouble(fields[3].trim());
        int speed = Integer.parseInt(fields[4].trim());
        FuelType fuel = FuelType.fromString(fields[5].trim());
        int horsepower = Integer.parseInt(fields[6].trim());
        double acceleration = Double.parseDouble(fields[7].trim());
        int flag = Integer.parseInt(fields[8].trim());
        if (flag != 0 && flag != 1) throw new IllegalArgumentException("Legacy status flag must be 0 or 1: " + flag);
        Status status = flag == 1 ? Status.AVAILABLE : Status.SOLD;
        return new Car(id, brand, model, price, speed, fuel, horsepower, acceleration, status);
    }
}
