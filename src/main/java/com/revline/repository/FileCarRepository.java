package com.revline.repository;

import com.revline.model.Car;
import com.revline.model.Status;
import com.revline.util.FileUtil;
import com.revline.util.exception.DataFormatException;
import com.revline.util.exception.RepositoryException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * File-based implementation of CarRepository storing records in CSV format (L26, L27).
 * Uses character streams for I/O and crash-safe file replacement.
 */
public class FileCarRepository implements CarRepository {

    public static final String CSV_HEADER = "id,brand,model,price,topSpeed,fuelType,horsepower,acceleration,status";

    private final File file;
    private final List<Car> cars;
    private final List<String> lastLoadReport;

    public FileCarRepository(File file) throws RepositoryException {
        this.file = file;
        this.cars = new ArrayList<>();
        this.lastLoadReport = new ArrayList<>();
        load();
    }

    /**
     * Reads all records from disk CSV.
     */
    public final synchronized void load() throws RepositoryException {
        cars.clear();
        lastLoadReport.clear();

        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Header
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    Car car = Car.fromCsv(line, lineNumber);
                    cars.add(car);
                } catch (DataFormatException e) {
                    lastLoadReport.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to read cars file: " + file.getName(), e);
        }
    }

    private synchronized void persistAll() throws RepositoryException {
        List<String> lines = new ArrayList<>();
        lines.add(CSV_HEADER);
        for (Car c : cars) {
            lines.add(c.toCsv());
        }
        FileUtil.safeWrite(file, lines);
    }

    @Override
    public synchronized List<Car> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(cars));
    }

    @Override
    public synchronized Car findById(Integer id) {
        if (id == null) return null;
        for (Car c : cars) {
            if (c.getId() == id) {
                return c.copy();
            }
        }
        return null;
    }

    @Override
    public synchronized void save(Car car) throws RepositoryException {
        if (car == null) return;
        boolean updated = false;
        for (int i = 0; i < cars.size(); i++) {
            if (cars.get(i).getId() == car.getId()) {
                cars.set(i, car.copy());
                updated = true;
                break;
            }
        }
        if (!updated) {
            cars.add(car.copy());
        }
        persistAll();
    }

    @Override
    public synchronized boolean deleteById(Integer id) throws RepositoryException {
        if (id == null) return false;
        Car target = null;
        for (Car c : cars) {
            if (c.getId() == id) {
                target = c;
                break;
            }
        }
        if (target != null) {
            cars.remove(target);
            persistAll();
            return true;
        }
        return false;
    }

    @Override
    public synchronized void saveAll(List<Car> all) throws RepositoryException {
        cars.clear();
        if (all != null) {
            for (Car c : all) {
                cars.add(c.copy());
            }
        }
        persistAll();
    }

    @Override
    public synchronized int count() {
        return cars.size();
    }

    @Override
    public synchronized List<Car> findByStatus(Status status) {
        List<Car> result = new ArrayList<>();
        for (Car c : cars) {
            if (c.getStatus() == status) {
                result.add(c.copy());
            }
        }
        return result;
    }

    @Override
    public synchronized List<Car> findByPriceRange(double minPrice, double maxPrice) {
        List<Car> result = new ArrayList<>();
        for (Car c : cars) {
            if (c.getPrice() >= minPrice && c.getPrice() <= maxPrice) {
                result.add(c.copy());
            }
        }
        return result;
    }

    @Override
    public synchronized int nextId() {
        int max = 0;
        for (Car c : cars) {
            if (c.getId() > max) {
                max = c.getId();
            }
        }
        return max + 1;
    }

    public List<String> getLastLoadReport() {
        return Collections.unmodifiableList(new ArrayList<>(lastLoadReport));
    }

    public File getFile() {
        return file;
    }
}
