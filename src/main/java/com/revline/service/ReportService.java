package com.revline.service;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Sale;
import com.revline.model.Status;
import com.revline.repository.SaleRepository;
import com.revline.util.Pair;
import com.revline.util.exception.RepositoryException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Stream-based showroom reports for inventory analytics (L21-L23).
 * Implements 15 analytical reporting pipelines (R1 through R15) using Java Streams,
 * lambdas, method references, and custom collectors.
 */
public class ReportService {

    public static final String[] PRICE_BAND_LABELS = {
            "< $500k",
            "$500k - $1M",
            "$1M - $2M",
            ">= $2M"
    };

    private final InventoryService inventoryService;
    private final SaleRepository saleRepository;

    public ReportService(InventoryService inventoryService) {
        this(inventoryService, null);
    }

    public ReportService(InventoryService inventoryService, SaleRepository saleRepository) {
        if (inventoryService == null) {
            throw new IllegalArgumentException("InventoryService cannot be null.");
        }
        this.inventoryService = inventoryService;
        this.saleRepository = saleRepository;
    }

    // =========================================================================
    // R1: Available cars (filter)
    // =========================================================================
    public List<Car> getAvailableCars() {
        return inventoryService.findAll().stream()
                .filter(car -> car.getStatus() == Status.AVAILABLE)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // R2: Stock value of available cars (filter -> mapToDouble -> sum)
    // =========================================================================
    public double calculateAvailableStockValue() {
        return inventoryService.findAll().stream()
                .filter(car -> car.getStatus() == Status.AVAILABLE)
                .mapToDouble(Car::getPrice)
                .sum();
    }

    // =========================================================================
    // R3: Portfolio value of all cars in inventory
    // =========================================================================
    public double calculateTotalPortfolioValue() {
        return inventoryService.findAll().stream()
                .mapToDouble(Car::getPrice)
                .sum();
    }

    // =========================================================================
    // R4: Cars per fuel type (groupingBy + counting into EnumMap)
    // =========================================================================
    public Map<FuelType, Long> countCarsByFuelType() {
        Map<FuelType, Long> rawCounts = inventoryService.findAll().stream()
                .collect(Collectors.groupingBy(Car::getFuelType, Collectors.counting()));

        Map<FuelType, Long> fullMap = new EnumMap<>(FuelType.class);
        for (FuelType ft : FuelType.values()) {
            fullMap.put(ft, rawCounts.getOrDefault(ft, 0L));
        }
        return fullMap;
    }

    // =========================================================================
    // R5: Top-3 fastest cars (sorted by top speed descending -> limit(3))
    // =========================================================================
    public List<Car> getTop3FastestCars() {
        return inventoryService.findAll().stream()
                .sorted(Comparator.comparingInt(Car::getTopSpeed).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // R6: Top-N by horsepower (filter non-sold -> sorted -> limit -> map)
    // =========================================================================
    public List<String> topNByHorsepower(int n) {
        if (n <= 0) {
            return Collections.emptyList();
        }
        return inventoryService.findAll().stream()
                .filter(car -> car.getStatus() != Status.SOLD)
                .sorted((first, second) -> Integer.compare(second.getHorsepower(), first.getHorsepower()))
                .limit(n)
                .map(Car::describe)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // R7: Average price by brand (groupingBy brand + averagingDouble)
    // =========================================================================
    public Map<String, Double> averagePriceByBrand() {
        return inventoryService.findAll().stream()
                .collect(Collectors.groupingBy(Car::getBrand,
                        Collectors.averagingDouble(Car::getPrice)));
    }

    // =========================================================================
    // R8: Distinct brands sorted (map -> distinct -> sorted)
    // =========================================================================
    public List<String> getDistinctBrandsSorted() {
        return inventoryService.findAll().stream()
                .map(Car::getBrand)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // =========================================================================
    // R9: Pagination of inventory (sorted by ID -> skip -> limit)
    // =========================================================================
    public List<Car> getCarsPaged(int page, int pageSize) {
        if (page <= 0 || pageSize <= 0) {
            return Collections.emptyList();
        }
        return inventoryService.findAll().stream()
                .sorted(Comparator.comparingInt(Car::getId))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // R10: Total sales revenue (sales.stream().mapToDouble.sum())
    // =========================================================================
    public double calculateTotalRevenue() {
        return getSales().stream()
                .mapToDouble(Sale::getPrice)
                .sum();
    }

    // =========================================================================
    // R11: Revenue by buyer (groupingBy buyer -> summingDouble)
    // =========================================================================
    public Map<String, Double> getRevenueByBuyer() {
        return getSales().stream()
                .collect(Collectors.groupingBy(Sale::getBuyerName,
                        Collectors.summingDouble(Sale::getPrice)));
    }

    // =========================================================================
    // R12: Fastest 0-100 acceleration per fuel type (groupingBy + minBy)
    // Optional represents "maybe a value" produced by Collectors.minBy on possibly empty groups.
    // Callers safely unwrap via isPresent() and get() to inspect the winning vehicle.
    // =========================================================================
    public Map<FuelType, Optional<Car>> getFastestPerFuelType() {
        return inventoryService.findAll().stream()
                .collect(Collectors.groupingBy(Car::getFuelType,
                        Collectors.minBy(Comparator.comparingDouble(Car::getAcceleration))));
    }

    // =========================================================================
    // R13: Price band matrix (2-D array int[fuel][band])
    // =========================================================================
    public static int getPriceBandIndex(double price) {
        if (price < 500_000.0) return 0;
        if (price < 1_000_000.0) return 1;
        if (price < 2_000_000.0) return 2;
        return 3;
    }

    public int[][] getPriceBandMatrix() {
        int[][] matrix = new int[FuelType.values().length][4];
        for (Car car : inventoryService.findAll()) {
            int fuelIndex = car.getFuelType().ordinal();
            int bandIndex = getPriceBandIndex(car.getPrice());
            matrix[fuelIndex][bandIndex]++;
        }
        return matrix;
    }

    public String formatPriceBandMatrix() {
        return ReportSummaryFormatter.formatPriceBandMatrix(getPriceBandMatrix());
    }

    // =========================================================================
    // R14: Formatted vehicle names list (joining(", "))
    // =========================================================================
    public String getFormattedVehicleNames() {
        return inventoryService.findAll().stream()
                .map(car -> car.getBrand() + " " + car.getModel())
                .collect(Collectors.joining(", "));
    }

    // =========================================================================
    // R15: Stream intermediate laziness demo using peek counters
    // =========================================================================
    public Pair<Integer, Integer> demonstrateLaziness(int limit) {
        final int[] unconsumedCounter = new int[1];
        inventoryService.findAll().stream()
                .peek(c -> unconsumedCounter[0]++);

        final int[] consumedCounter = new int[1];
        inventoryService.findAll().stream()
                .peek(c -> consumedCounter[0]++)
                .limit(Math.max(0, limit))
                .collect(Collectors.toList());

        return Pair.of(unconsumedCounter[0], consumedCounter[0]);
    }

    // =========================================================================
    // Executive Summary Builder
    // =========================================================================
    public String generateAllReportsSummary() {
        return ReportSummaryFormatter.generateSummary(this);
    }

    private List<Sale> getSales() {
        if (saleRepository == null) {
            return Collections.emptyList();
        }
        try {
            return saleRepository.findAll();
        } catch (RepositoryException e) {
            return Collections.emptyList();
        }
    }
}
