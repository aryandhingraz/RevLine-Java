package com.revline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates that data/seed/cars.csv is 100% consistent with reference/c/cars.txt.
 * Verifies IDs, names, numbers, and that legacy sold cars (IDs 3, 4, 18) are marked SOLD.
 */
public class SeedConsistencyTest {

    private static class LegacyCarRow {
        int id;
        String brand;
        String model;
        double price;
        int topSpeed;
        String fuelType;
        int horsepower;
        double acceleration;
        int isAvailable;
    }

    private static class SeedCarRow {
        int id;
        String brand;
        String model;
        double price;
        int topSpeed;
        String fuelType;
        int horsepower;
        double acceleration;
        String status;
    }

    public static void runAll() {
        testSeedMatchesLegacyC();
        testLegacySoldCarsPreserved();
        testSeedSalesConsistency();
    }

    public static void testSeedMatchesLegacyC() {
        File cFile = new File("reference/c/cars.txt");
        File csvFile = new File("data/seed/cars.csv");

        Assert.assertTrue(cFile.exists(), "Legacy C cars.txt must exist at reference/c/cars.txt");
        Assert.assertTrue(csvFile.exists(), "Seed cars.csv must exist at data/seed/cars.csv");

        List<LegacyCarRow> legacyRows = loadLegacy(cFile);
        List<SeedCarRow> seedRows = loadSeed(csvFile);

        Assert.assertEquals(18, legacyRows.size(), "Legacy file must contain exactly 18 cars");
        Assert.assertEquals(18, seedRows.size(), "Seed CSV must contain exactly 18 cars");

        for (int i = 0; i < 18; i++) {
            LegacyCarRow leg = legacyRows.get(i);
            SeedCarRow seed = seedRows.get(i);

            Assert.assertEquals(leg.id, seed.id, "Car ID mismatch at index " + i);
            Assert.assertEquals(leg.brand, seed.brand, "Car brand mismatch for ID " + leg.id);
            Assert.assertEquals(leg.model, seed.model, "Car model mismatch for ID " + leg.id);
            Assert.assertEquals(leg.price, seed.price, 0.01, "Price mismatch for ID " + leg.id);
            Assert.assertEquals(leg.topSpeed, seed.topSpeed, "Top speed mismatch for ID " + leg.id);
            Assert.assertTrue(leg.fuelType.equalsIgnoreCase(seed.fuelType),
                    "Fuel type mismatch for ID " + leg.id + ": legacy=" + leg.fuelType + ", seed=" + seed.fuelType);
            Assert.assertEquals(leg.horsepower, seed.horsepower, "Horsepower mismatch for ID " + leg.id);
            Assert.assertEquals(leg.acceleration, seed.acceleration, 0.01, "Acceleration mismatch for ID " + leg.id);

            String expectedStatus = (leg.isAvailable == 1) ? "AVAILABLE" : "SOLD";
            Assert.assertEquals(expectedStatus, seed.status, "Status mismatch for ID " + leg.id);
        }
    }

    public static void testLegacySoldCarsPreserved() {
        File csvFile = new File("data/seed/cars.csv");
        List<SeedCarRow> seedRows = loadSeed(csvFile);

        // IDs 3 (Ferrari LaFerrari), 4 (Ferrari SF90), 18 (Audi R8) must be SOLD
        for (SeedCarRow row : seedRows) {
            if (row.id == 3 || row.id == 4 || row.id == 18) {
                Assert.assertEquals("SOLD", row.status, "Car #" + row.id + " must be marked SOLD");
            } else {
                Assert.assertEquals("AVAILABLE", row.status, "Car #" + row.id + " must be marked AVAILABLE");
            }
        }
    }

    public static void testSeedSalesConsistency() {
        File salesFile = new File("data/seed/sales.csv");
        Assert.assertTrue(salesFile.exists(), "sales.csv must exist");

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(salesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading sales.csv", e);
        }

        // Header + 3 sales for cars 3, 4, 18
        Assert.assertEquals(4, lines.size(), "sales.csv must have header + 3 rows");
        Assert.assertTrue(lines.get(1).contains(",3,"), "Sale 1 must link to car ID 3");
        Assert.assertTrue(lines.get(2).contains(",4,"), "Sale 2 must link to car ID 4");
        Assert.assertTrue(lines.get(3).contains(",18,"), "Sale 3 must link to car ID 18");
    }

    private static List<LegacyCarRow> loadLegacy(File file) {
        List<LegacyCarRow> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", -1);
                LegacyCarRow r = new LegacyCarRow();
                r.id = Integer.parseInt(p[0].trim());
                r.brand = p[1].trim();
                r.model = p[2].trim();
                r.price = Double.parseDouble(p[3].trim());
                r.topSpeed = Integer.parseInt(p[4].trim());
                r.fuelType = p[5].trim();
                r.horsepower = Integer.parseInt(p[6].trim());
                r.acceleration = Double.parseDouble(p[7].trim());
                r.isAvailable = Integer.parseInt(p[8].trim());
                list.add(r);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading legacy file", e);
        }
        return list;
    }

    private static List<SeedCarRow> loadSeed(File file) {
        List<SeedCarRow> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", -1);
                SeedCarRow r = new SeedCarRow();
                r.id = Integer.parseInt(p[0].trim());
                r.brand = p[1].trim();
                r.model = p[2].trim();
                r.price = Double.parseDouble(p[3].trim());
                r.topSpeed = Integer.parseInt(p[4].trim());
                r.fuelType = p[5].trim();
                r.horsepower = Integer.parseInt(p[6].trim());
                r.acceleration = Double.parseDouble(p[7].trim());
                r.status = p[8].trim();
                list.add(r);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading seed CSV", e);
        }
        return list;
    }
}
