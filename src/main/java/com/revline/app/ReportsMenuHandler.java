package com.revline.app;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.service.ReportService;
import com.revline.ui.InputReader;
import com.revline.ui.MenuPrinter;
import com.revline.util.FileUtil;
import com.revline.util.Pair;
import com.revline.util.exception.RepositoryException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles executive analytics and reports submenu interactions (R1-R15).
 */
final class ReportsMenuHandler {

    private final ReportService reportService;
    private final InputReader reader;

    ReportsMenuHandler(ReportService reportService, InputReader reader) {
        this.reportService = reportService;
        this.reader = reader;
    }

    void handleReportsMenu() {
        boolean inReports = true;
        while (inReports) {
            MenuPrinter.printReportsMenu();
            String choice = reader.readLine("  Select Option (0-7): ");
            switch (choice) {
                case "1":
                    System.out.println();
                    System.out.print(reportService.generateAllReportsSummary());
                    break;
                case "2":
                    System.out.println();
                    System.out.println("--- PRICE BAND DISTRIBUTION MATRIX (R13: int[3][4]) ---");
                    System.out.print(reportService.formatPriceBandMatrix());
                    break;
                case "3": handleTopNByHorsepower(); break;
                case "4": handleAveragePriceByBrand(); break;
                case "5": handleFastestPerFuelType(); break;
                case "6": handleStreamLazinessDemo(); break;
                case "7": handleExportReport(); break;
                case "0": inReports = false; break;
                default: System.out.println("  [!] Invalid choice. Please enter 0-7.");
            }
        }
    }

    private void handleTopNByHorsepower() {
        int n = reader.readInt("  Enter N (number of top horsepower cars to list): ", 1, 50);
        List<String> topCars = reportService.topNByHorsepower(n);
        System.out.println();
        System.out.println("--- TOP-" + n + " SUPERCAR POWER RANKINGS (EX-4 / R6) ---");
        if (topCars.isEmpty()) {
            System.out.println("  No active vehicles available.");
        } else {
            for (int i = 0; i < topCars.size(); i++) {
                System.out.printf("  %2d. %s\n", i + 1, topCars.get(i));
            }
        }
    }

    private void handleAveragePriceByBrand() {
        Map<String, Double> brandAvgs = reportService.averagePriceByBrand();
        System.out.println();
        System.out.println("--- AVERAGE PRICE BY BRAND (EX-5 / R7) ---");
        for (Map.Entry<String, Double> entry : brandAvgs.entrySet()) {
            System.out.printf("  - %-15s : $%,.2f\n", entry.getKey(), entry.getValue());
        }
    }

    private void handleFastestPerFuelType() {
        Map<FuelType, Optional<Car>> fastestMap = reportService.getFastestPerFuelType();
        System.out.println();
        System.out.println("--- FASTEST 0-100 KM/H ACCELERATION PER FUEL TYPE (R12) ---");
        for (FuelType ft : FuelType.values()) {
            Optional<Car> opt = fastestMap.get(ft);
            String details = opt.map(c -> String.format("%s %s (%.2f s, Top Speed %d km/h, $%,.2f)",
                    c.getBrand(), c.getModel(), c.getAcceleration(), c.getTopSpeed(), c.getPrice()))
                    .orElse("No vehicles in category");
            System.out.printf("  - %-10s : %s\n", ft.label(), details);
        }
    }

    private void handleStreamLazinessDemo() {
        int limit = reader.readInt("  Enter stream short-circuit limit (e.g. 3): ", 1, 50);
        Pair<Integer, Integer> result = reportService.demonstrateLaziness(limit);
        System.out.println();
        System.out.println("--- STREAM INTERMEDIATE LAZINESS DEMONSTRATION (R15 - L22, L23) ---");
        System.out.printf("  1. Unconsumed Stream Pipeline (No Terminal Op): %d elements processed (Laziness proven)\n",
                result.getFirst());
        System.out.printf("  2. Consumed Stream with limit(%d) Terminal Op : %d elements processed (Short-circuit proven)\n",
                limit, result.getSecond());
        System.out.println("  Explanation: Intermediate operations (.peek) are never evaluated until a terminal");
        System.out.println("  operation is invoked, and limit() short-circuits traversal as soon as requested items match.");
    }

    private void handleExportReport() {
        String report = reportService.generateAllReportsSummary();
        File exportDir = new File("data/exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File targetFile = new File(exportDir, "report_summary.txt");
        try {
            List<String> lines = Arrays.asList(report.split("\r?\n"));
            FileUtil.safeWrite(targetFile, lines);
            System.out.println("\n  [✓] Executive Analytics Report exported successfully!");
            System.out.println("      Location : " + targetFile.getAbsolutePath());
            System.out.printf("      Lines    : %d lines written via FileUtil.safeWrite\n", lines.size());
        } catch (RepositoryException e) {
            System.out.println("\n  [!] Failed to export report: " + e.getMessage());
        }
    }
}
