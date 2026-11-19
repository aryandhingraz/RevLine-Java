package com.revline.app;

import com.revline.model.Car;
import com.revline.service.DataToolsService;
import com.revline.service.InventoryService;
import com.revline.service.SalesService;
import com.revline.ui.InputReader;
import com.revline.ui.MenuPrinter;

import java.io.File;
import java.util.List;

/**
 * Handles data tools submenu interactions (legacy import, csv import, scripts, backup/restore, synthetic).
 */
final class DataToolsMenuHandler {

    private final DataToolsService dataToolsService;
    private final InventoryService inventoryService;
    private final SalesService salesService;
    private final InputReader reader;

    DataToolsMenuHandler(DataToolsService dataToolsService,
                         InventoryService inventoryService,
                         SalesService salesService,
                         InputReader reader) {
        this.dataToolsService = dataToolsService;
        this.inventoryService = inventoryService;
        this.salesService = salesService;
        this.reader = reader;
    }

    void handleDataToolsMenu() {
        boolean inTools = true;
        while (inTools) {
            MenuPrinter.printDataToolsMenu();
            String choice = reader.readLine("  Select Option (0-6): ");
            switch (choice) {
                case "1": handleLegacyImport(); break;
                case "2": handleCsvBatchImport(); break;
                case "3": handleScriptImport(); break;
                case "4": handleDatabaseBackup(); break;
                case "5": handleDatabaseRestore(); break;
                case "6": handleSyntheticGeneration(); break;
                case "0": inTools = false; break;
                default: System.out.println("  [!] Invalid choice. Please enter 0-6.");
            }
        }
    }

    private void handleLegacyImport() {
        File file = new File("data/legacy/cars.txt");
        if (!file.exists()) {
            System.out.println("  [!] Legacy file not found at " + file.getPath());
            return;
        }
        try {
            DataToolsService.LegacyImportResult res = dataToolsService.importLegacyC(file, inventoryService);
            System.out.println("\n  [✓] Legacy C Import Complete: " + res);
        } catch (Exception e) {
            System.out.println("\n  [!] Legacy Import failed: " + e.getMessage());
        }
    }

    private void handleCsvBatchImport() {
        String path = reader.readLine("  Enter CSV file path (or press Enter for sample 'data/sample/import_bad_rows.csv'): ");
        if (path.isEmpty()) {
            path = "data/sample/import_bad_rows.csv";
        }
        File csvFile = new File(path);
        if (!csvFile.exists()) {
            System.out.println("  [!] File not found: " + path);
            return;
        }
        try {
            File errDir = new File("data/reports");
            DataToolsService.CsvImportResult res = dataToolsService.importCsv(csvFile, inventoryService, errDir);
            System.out.println("\n  [✓] " + res);
            if (res.getErrorReportFile() != null) {
                System.out.println("      Error details saved to: " + res.getErrorReportFile().getPath());
            }
        } catch (Exception e) {
            System.out.println("\n  [!] CSV Import failed: " + e.getMessage());
        }
    }

    private void handleScriptImport() {
        String path = reader.readLine("  Enter script file path (or press Enter for 'data/sample/bulk_import.txt'): ");
        if (path.isEmpty()) {
            path = "data/sample/bulk_import.txt";
        }
        File scriptFile = new File(path);
        if (!scriptFile.exists()) {
            System.out.println("  [!] Script file not found: " + path);
            return;
        }
        try {
            DataToolsService.ScriptImportResult res = dataToolsService.importScript(scriptFile, inventoryService, salesService);
            System.out.println("\n  [✓] StreamTokenizer Script Batch Finished:");
            System.out.printf("      Commands Executed: %d | Errors Caught: %d\n", res.getExecutedCommands(), res.getFailedCommands());
            for (String logLine : res.getExecutionLog()) {
                System.out.println("      - " + logLine);
            }
        } catch (Exception e) {
            System.out.println("\n  [!] Script import failed: " + e.getMessage());
        }
    }

    private void handleDatabaseBackup() {
        File src = new File("data/cars.csv");
        if (!src.exists()) {
            src = new File("data/seed/cars.csv");
        }
        File backupDir = new File("data/backups");
        if (!backupDir.exists()) backupDir.mkdirs();
        File backupFile = new File(backupDir, "cars_backup_" + System.currentTimeMillis() + ".bak");
        try {
            dataToolsService.backupFile(src, backupFile);
            System.out.println("\n  [✓] Lossless byte-stream backup created successfully!");
            System.out.println("      Location: " + backupFile.getAbsolutePath() + " (" + backupFile.length() + " bytes)");
        } catch (Exception e) {
            System.out.println("\n  [!] Backup failed: " + e.getMessage());
        }
    }

    private void handleDatabaseRestore() {
        File backupDir = new File("data/backups");
        File[] backups = backupDir.listFiles((dir, name) -> name.endsWith(".bak"));
        if (backups == null || backups.length == 0) {
            System.out.println("\n  [!] No backup files found in " + backupDir.getPath());
            return;
        }
        System.out.println("\nAvailable Backups:");
        for (int i = 0; i < backups.length; i++) {
            System.out.printf("  [%d] %s (%d bytes)\n", i + 1, backups[i].getName(), backups[i].length());
        }
        int sel = reader.readInt("Select backup to restore (1-" + backups.length + "): ", 1, backups.length);
        File chosen = backups[sel - 1];
        File target = new File("data/cars.csv");
        try {
            dataToolsService.restoreFile(chosen, target);
            System.out.println("  [✓] Restore complete from " + chosen.getName() + " -> " + target.getName());
        } catch (Exception e) {
            System.out.println("  [!] Restore failed: " + e.getMessage());
        }
    }

    private void handleSyntheticGeneration() {
        int count = reader.readInt("  Enter number of synthetic supercars to generate (e.g. 50): ", 1, 1000);
        List<Car> generated = dataToolsService.generateSyntheticCars(count);
        int added = 0;
        for (Car c : generated) {
            try {
                inventoryService.addCar(c);
                added++;
            } catch (Exception ignored) {}
        }
        System.out.printf("\n  [✓] Generated and added %d synthetic test supercars into inventory.\n", added);
        System.out.printf("      Showroom fleet size is now: %d vehicles.\n", inventoryService.count());
    }
}
