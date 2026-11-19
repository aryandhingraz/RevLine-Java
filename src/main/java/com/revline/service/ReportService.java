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
    // R6: Top-N by horsepower (EX-4: filter non-sold -> sorted -> limit -> map)
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
    // R7: Average price by brand (EX-5: groupingBy brand + averagingDouble)
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
        int[][] matrix = getPriceBandMatrix();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-12s | %-12s | %-14s | %-14s | %-10s%n",
                "Fuel Type", PRICE_BAND_LABELS[0], PRICE_BAND_LABELS[1], PRICE_BAND_LABELS[2], PRICE_BAND_LABELS[3]));
        sb.append("-------------+--------------+----------------+----------------+-----------").append(System.lineSeparator());
        for (FuelType ft : FuelType.values()) {
            int row = ft.ordinal();
            sb.append(String.format("%-12s | %-12d | %-14d | %-14d | %-10d%n",
                    ft.label(), matrix[row][0], matrix[row][1], matrix[row][2], matrix[row][3]));
        }
        return sb.toString();
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
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================================").append(System.lineSeparator());
        sb.append("                 REVLINE SHOWROOM EXECUTIVE ANALYTICS REPORT              ").append(System.lineSeparator());
        sb.append("==========================================================================").append(System.lineSeparator());

        sb.append(String.format("[R1]  Available Vehicles        : %d%n", getAvailableCars().size()));
        sb.append(String.format("[R2]  Stock Value (Available)  : $%,.2f%n", calculateAvailableStockValue()));
        sb.append(String.format("[R3]  Total Portfolio Value    : $%,.2f%n", calculateTotalPortfolioValue()));

        sb.append("[R4]  Vehicles Per Fuel Type    : ");
        Map<FuelType, Long> fuelCounts = countCarsByFuelType();
        sb.append(String.format("PETROL: %d | HYBRID: %d | ELECTRIC: %d%n",
                fuelCounts.get(FuelType.PETROL), fuelCounts.get(FuelType.HYBRID), fuelCounts.get(FuelType.ELECTRIC)));

        sb.append("[R5]  Top-3 Fastest Vehicles    :").append(System.lineSeparator());
        List<Car> top3 = getTop3FastestCars();
        for (int i = 0; i < top3.size(); i++) {
            Car c = top3.get(i);
            sb.append(String.format("      %d. %s %s - %d km/h ($%,.2f)%n", i + 1, c.getBrand(), c.getModel(), c.getTopSpeed(), c.getPrice()));
        }

        sb.append("[R6]  Top-3 By Horsepower (EX-4):").append(System.lineSeparator());
        List<String> topHp = topNByHorsepower(3);
        for (int i = 0; i < topHp.size(); i++) {
            sb.append(String.format("      %d. %s%n", i + 1, topHp.get(i)));
        }

        sb.append("[R7]  Average Price By Brand    :").append(System.lineSeparator());
        Map<String, Double> brandAvgs = averagePriceByBrand();
        for (Map.Entry<String, Double> entry : brandAvgs.entrySet()) {
            sb.append(String.format("      - %-15s : $%,.2f%n", entry.getKey(), entry.getValue()));
        }

        sb.append(String.format("[R8]  Distinct Brands (%d)       : %s%n",
                getDistinctBrandsSorted().size(), String.join(", ", getDistinctBrandsSorted())));

        sb.append("[R9]  Inventory Page 1 (size 5) :").append(System.lineSeparator());
        List<Car> page1 = getCarsPaged(1, 5);
        for (Car c : page1) {
            sb.append(String.format("      #%d %s %s ($%,.2f)%n", c.getId(), c.getBrand(), c.getModel(), c.getPrice()));
        }

        sb.append(String.format("[R10] Total Realized Revenue    : $%,.2f%n", calculateTotalRevenue()));

        sb.append("[R11] Revenue By Buyer          :").append(System.lineSeparator());
        Map<String, Double> buyerRev = getRevenueByBuyer();
        if (buyerRev.isEmpty()) {
            sb.append("      (No sales transactions recorded)%n");
        } else {
            for (Map.Entry<String, Double> entry : buyerRev.entrySet()) {
                sb.append(String.format("      - %-15s : $%,.2f%n", entry.getKey(), entry.getValue()));
            }
        }

        sb.append("[R12] Fastest 0-100 By Fuel Type:").append(System.lineSeparator());
        Map<FuelType, Optional<Car>> fastestPerFuel = getFastestPerFuelType();
        for (Map.Entry<FuelType, Optional<Car>> entry : fastestPerFuel.entrySet()) {
            String details = entry.getValue()
                    .map(c -> String.format("%s %s (%.2f s)", c.getBrand(), c.getModel(), c.getAcceleration()))
                    .orElse("None");
            sb.append(String.format("      - %-10s : %s%n", entry.getKey().label(), details));
        }

        sb.append("[R13] Price Band Distribution   :").append(System.lineSeparator());
        sb.append(formatPriceBandMatrix());

        sb.append("[R14] Showroom Lineup Names     :").append(System.lineSeparator());
        sb.append("      ").append(getFormattedVehicleNames()).append(System.lineSeparator());

        Pair<Integer, Integer> laziness = demonstrateLaziness(3);
        sb.append(String.format("[R15] Stream Laziness Demo      : Unconsumed peek count = %d, Short-circuit count (limit 3) = %d%n",
                laziness.getFirst(), laziness.getSecond()));

        sb.append("==========================================================================").append(System.lineSeparator());
        return sb.toString();
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

    public static void main(String[] args) {
        try {
            com.revline.repository.FileCarRepository carRepo =
                    new com.revline.repository.FileCarRepository(new java.io.File("data/seed/cars.csv"));
            com.revline.repository.FileSaleRepository saleRepo =
                    new com.revline.repository.FileSaleRepository(new java.io.File("data/seed/sales.csv"));
            InventoryService inv = new InventoryService(carRepo, null);
            ReportService service = new ReportService(inv, saleRepo);
            String summary = service.generateAllReportsSummary();
            System.out.print(summary);

            java.io.File exportDir = new java.io.File("data/exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            java.io.File exportFile = new java.io.File(exportDir, "report_summary.txt");
            com.revline.util.FileUtil.safeWrite(exportFile, java.util.Arrays.asList(summary.split("\r?\n")));
            System.out.println("  [OK] Report successfully written to " + exportFile.getPath());
        } catch (Exception e) {
            System.err.println("Failed to run ReportService: " + e.getMessage());
        }
    }
}
