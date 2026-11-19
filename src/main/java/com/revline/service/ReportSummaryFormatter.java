package com.revline.service;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Formatter for showroom analytical reports and executive summaries (L21–L24).
 */
public final class ReportSummaryFormatter {

    private ReportSummaryFormatter() {}

    public static String formatPriceBandMatrix(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-12s | %-12s | %-14s | %-14s | %-10s%n",
                "Fuel Type", ReportService.PRICE_BAND_LABELS[0], ReportService.PRICE_BAND_LABELS[1],
                ReportService.PRICE_BAND_LABELS[2], ReportService.PRICE_BAND_LABELS[3]));
        sb.append("-------------+--------------+----------------+----------------+-----------").append(System.lineSeparator());
        for (FuelType ft : FuelType.values()) {
            int row = ft.ordinal();
            sb.append(String.format("%-12s | %-12d | %-14d | %-14d | %-10d%n",
                    ft.label(), matrix[row][0], matrix[row][1], matrix[row][2], matrix[row][3]));
        }
        return sb.toString();
    }

    public static String generateSummary(ReportService service) {
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================================================").append(System.lineSeparator());
        sb.append("                 REVLINE SHOWROOM EXECUTIVE ANALYTICS REPORT              ").append(System.lineSeparator());
        sb.append("==========================================================================").append(System.lineSeparator());

        sb.append(String.format("[R1]  Available Vehicles        : %d%n", service.getAvailableCars().size()));
        sb.append(String.format("[R2]  Stock Value (Available)  : $%,.2f%n", service.calculateAvailableStockValue()));
        sb.append(String.format("[R3]  Total Portfolio Value    : $%,.2f%n", service.calculateTotalPortfolioValue()));

        sb.append("[R4]  Vehicles Per Fuel Type    : ");
        Map<FuelType, Long> fuelCounts = service.countCarsByFuelType();
        sb.append(String.format("PETROL: %d | HYBRID: %d | ELECTRIC: %d%n",
                fuelCounts.get(FuelType.PETROL), fuelCounts.get(FuelType.HYBRID), fuelCounts.get(FuelType.ELECTRIC)));

        sb.append("[R5]  Top-3 Fastest Vehicles    :").append(System.lineSeparator());
        List<Car> top3 = service.getTop3FastestCars();
        for (int i = 0; i < top3.size(); i++) {
            Car c = top3.get(i);
            sb.append(String.format("      %d. %s %s - %d km/h ($%,.2f)%n", i + 1, c.getBrand(), c.getModel(), c.getTopSpeed(), c.getPrice()));
        }

        sb.append("[R6]  Top-3 By Horsepower (EX-4):").append(System.lineSeparator());
        List<String> topHp = service.topNByHorsepower(3);
        for (int i = 0; i < topHp.size(); i++) {
            sb.append(String.format("      %d. %s%n", i + 1, topHp.get(i)));
        }

        sb.append("[R7]  Average Price By Brand    :").append(System.lineSeparator());
        Map<String, Double> brandAvgs = service.averagePriceByBrand();
        for (Map.Entry<String, Double> entry : brandAvgs.entrySet()) {
            sb.append(String.format("      - %-15s : $%,.2f%n", entry.getKey(), entry.getValue()));
        }

        sb.append(String.format("[R8]  Distinct Brands (%d)       : %s%n",
                service.getDistinctBrandsSorted().size(), String.join(", ", service.getDistinctBrandsSorted())));

        sb.append("[R9]  Inventory Page 1 (size 5) :").append(System.lineSeparator());
        List<Car> page1 = service.getCarsPaged(1, 5);
        for (Car c : page1) {
            sb.append(String.format("      #%d %s %s ($%,.2f)%n", c.getId(), c.getBrand(), c.getModel(), c.getPrice()));
        }

        sb.append(String.format("[R10] Total Realized Revenue    : $%,.2f%n", service.calculateTotalRevenue()));

        sb.append("[R11] Revenue By Buyer          :").append(System.lineSeparator());
        Map<String, Double> buyerRev = service.getRevenueByBuyer();
        if (buyerRev.isEmpty()) {
            sb.append("      (No sales transactions recorded)%n");
        } else {
            for (Map.Entry<String, Double> entry : buyerRev.entrySet()) {
                sb.append(String.format("      - %-15s : $%,.2f%n", entry.getKey(), entry.getValue()));
            }
        }

        sb.append("[R12] Fastest 0-100 By Fuel Type:").append(System.lineSeparator());
        Map<FuelType, Optional<Car>> fastestPerFuel = service.getFastestPerFuelType();
        for (Map.Entry<FuelType, Optional<Car>> entry : fastestPerFuel.entrySet()) {
            String details = entry.getValue()
                    .map(c -> String.format("%s %s (%.2f s)", c.getBrand(), c.getModel(), c.getAcceleration()))
                    .orElse("None");
            sb.append(String.format("      - %-10s : %s%n", entry.getKey().label(), details));
        }

        sb.append("[R13] Price Band Distribution   :").append(System.lineSeparator());
        sb.append(service.formatPriceBandMatrix());

        sb.append("[R14] Showroom Lineup Names     :").append(System.lineSeparator());
        sb.append("      ").append(service.getFormattedVehicleNames()).append(System.lineSeparator());

        Pair<Integer, Integer> laziness = service.demonstrateLaziness(3);
        sb.append(String.format("[R15] Stream Laziness Demo      : Unconsumed peek count = %d, Short-circuit count (limit 3) = %d%n",
                laziness.getFirst(), laziness.getSecond()));

        sb.append("==========================================================================").append(System.lineSeparator());
        return sb.toString();
    }
}
