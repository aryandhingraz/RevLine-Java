package com.revline.app;

import com.revline.service.DataToolsService;
import com.revline.service.InventoryService;
import com.revline.service.SalesService;
import com.revline.ui.InputReader;
import com.revline.ui.MenuPrinter;
import com.revline.util.AppPaths;
import com.revline.util.exception.RepositoryException;

import java.io.File;
import java.util.List;

/**
 * Handles data tools submenu interactions (legacy import, csv import, scripts, byte-stream backup/restore).
 */
final class DataToolsMenuHandler {

    private final DataToolsService dataToolsService;
    private final InventoryService inventoryService;
    private final SalesService salesService;
    private final RevLineApp.StorageComponents storage;
    private final InputReader reader;

    DataToolsMenuHandler(DataToolsService dataToolsService,
                         InventoryService inventoryService,
                         SalesService salesService,
                         RevLineApp.StorageComponents storage,
                         InputReader reader) {
        this.dataToolsService = dataToolsService;
        this.inventoryService = inventoryService;
        this.salesService = salesService;
        this.storage = storage;
        this.reader = reader;
    }

    DataToolsMenuHandler(DataToolsService dataToolsService,
                         InventoryService inventoryService,
                         SalesService salesService,
                         InputReader reader) {
        this(dataToolsService, inventoryService, salesService, null, reader);
    }

    void handleDataToolsMenu() {
        boolean inTools = true;
        while (inTools) {
            MenuPrinter.printDataToolsMenu();
            String choice = reader.readLine("  Select Option (0-5): ");
            switch (choice) {
                case "1": handleLegacyImport(); break;
                case "2": handleCsvBatchImport(); break;
                case "3": handleScriptImport(); break;
                case "4": handleDatabaseBackup(); break;
                case "5": handleDatabaseRestore(); break;
                case "0": inTools = false; break;
                default: System.out.println("  [!] Invalid choice. Please enter 0-5.");
            }
        }
    }

    private void handleLegacyImport() {
        File file = new File(AppPaths.LEGACY_CARS);
        if (!file.exists()) {
            System.out.println("  [!] Legacy file not found at " + file.getPath());
            return;
        }
        try {
            DataToolsService.LegacyImportResult res = dataToolsService.importLegacyC(file, inventoryService);
            System.out.println("\n  [✓] Legacy Data Import Complete: " + res);
        } catch (Exception e) {
            System.out.println("\n  [!] Legacy Import failed: " + e.getMessage());
        }
    }

    private void handleCsvBatchImport() {
        String defaultPath = AppPaths.SAMPLE_DIR + "/import_bad_rows.csv";
        String path = reader.readCommand("  Enter CSV file path (or press Enter for sample '" + defaultPath + "'): ");
        if (path.isEmpty()) {
            path = defaultPath;
        }
        File csvFile = new File(path);
        if (!csvFile.exists()) {
            System.out.println("  [!] File not found: " + path);
            return;
        }
        try {
            File errDir = new File(AppPaths.REPORT_DIR);
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
        String defaultPath = AppPaths.SAMPLE_DIR + "/bulk_import.txt";
        String path = reader.readCommand("  Enter script file path (or press Enter for '" + defaultPath + "'): ");
        if (path.isEmpty()) {
            path = defaultPath;
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
        File backupDir = new File(AppPaths.BACKUP_DIR);
        if (!backupDir.exists()) backupDir.mkdirs();
        long ts = System.currentTimeMillis();
        File backupSet = new File(backupDir, "backup_" + ts);
        backupSet.mkdirs();

        int filesBackedUp = 0;
        File carsFile = new File(AppPaths.RUNTIME_CARS);
        if (!carsFile.exists()) {
            carsFile = new File(AppPaths.SEED_CARS);
        }
        if (carsFile.exists()) {
            try {
                dataToolsService.backupFile(carsFile, new File(backupSet, "cars.bak"));
                filesBackedUp++;
            } catch (Exception e) {
                System.out.println("  [!] Cars backup failed: " + e.getMessage());
            }
        }

        File salesFile = new File(AppPaths.RUNTIME_SALES);
        if (!salesFile.exists()) {
            salesFile = new File(AppPaths.SEED_SALES);
        }
        if (salesFile.exists()) {
            try {
                dataToolsService.backupFile(salesFile, new File(backupSet, "sales.bak"));
                filesBackedUp++;
            } catch (Exception e) {
                System.out.println("  [!] Sales backup failed: " + e.getMessage());
            }
        }

        File dbFile = new File(AppPaths.RUNTIME_DB);
        if (dbFile.exists() && dbFile.length() > 0) {
            boolean reconnectNeeded = false;
            try {
                if (storage != null && storage.isJdbcActive()) {
                    storage.closeDatabaseConnection();
                    reconnectNeeded = true;
                }
                dataToolsService.backupFile(dbFile, new File(backupSet, "revline.db.bak"));
                filesBackedUp++;
            } catch (Exception e) {
                System.out.println("  [!] Database backup failed: " + e.getMessage());
            } finally {
                if (reconnectNeeded) {
                    try {
                        storage.reconnectDatabase();
                    } catch (Exception e) {
                        System.out.println("  [!] Database reconnection failed: " + e.getMessage());
                    }
                }
            }
        }

        if (filesBackedUp > 0) {
            System.out.println("\n  [✓] Byte-stream backup created successfully!");
            System.out.println("      Backup Set: " + backupSet.getName() + " (" + filesBackedUp + " file(s) backed up)");
            System.out.println("      Location  : " + backupSet.getAbsolutePath());
        } else {
            System.out.println("\n  [!] No data files found to back up.");
        }
    }

    private void handleDatabaseRestore() {
        File backupDir = new File(AppPaths.BACKUP_DIR);
        File[] entries = backupDir.listFiles(f -> f.isDirectory() || f.getName().endsWith(".bak"));
        if (entries == null || entries.length == 0) {
            System.out.println("\n  [!] No backup files or sets found in " + backupDir.getPath());
            return;
        }
        System.out.println("\nAvailable Backups:");
        for (int i = 0; i < entries.length; i++) {
            File f = entries[i];
            if (f.isDirectory()) {
                File[] children = f.listFiles();
                int count = children != null ? children.length : 0;
                System.out.printf("  [%d] [SET] %s (%d files)\n", i + 1, f.getName(), count);
            } else {
                System.out.printf("  [%d] [FILE] %s (%d bytes)\n", i + 1, f.getName(), f.length());
            }
        }
        int sel = reader.readInt("Select backup to restore (1-" + entries.length + "): ", 1, entries.length);
        File chosen = entries[sel - 1];
        String confirmation = reader.readLine(
                "  Restoring replaces current application data. Type RESTORE to continue: ");
        if (!"RESTORE".equals(confirmation.trim())) {
            System.out.println("  Restore cancelled. No files were changed.");
            return;
        }

        try {
            if (chosen.isDirectory()) {
                File carsBak = new File(chosen, "cars.bak");
                File salesBak = new File(chosen, "sales.bak");
                File dbBak = new File(chosen, "revline.db.bak");
                boolean hasCsvPair = carsBak.isFile() && salesBak.isFile();
                boolean hasDatabase = dbBak.isFile();
                if (!hasCsvPair && !hasDatabase) {
                    throw new IllegalArgumentException(
                            "Backup set is incomplete. It must contain both cars.bak and sales.bak, or revline.db.bak.");
                }
                if (carsBak.exists() != salesBak.exists()) {
                    throw new IllegalArgumentException("CSV backup is incomplete: both cars and sales files are required.");
                }
                boolean shouldRestoreDb = dbBak.exists();
                boolean reconnectNeeded = false;
                try {
                    if (shouldRestoreDb && storage != null && storage.isJdbcActive()) {
                        storage.closeDatabaseConnection();
                        reconnectNeeded = true;
                    }
                    if (carsBak.exists()) {
                        dataToolsService.restoreFile(carsBak, new File(AppPaths.RUNTIME_CARS));
                        System.out.println("  [✓] Restored cars data to " + AppPaths.RUNTIME_CARS);
                    }
                    if (salesBak.exists()) {
                        dataToolsService.restoreFile(salesBak, new File(AppPaths.RUNTIME_SALES));
                        System.out.println("  [✓] Restored sales data to " + AppPaths.RUNTIME_SALES);
                    }
                    if (shouldRestoreDb) {
                        dataToolsService.restoreFile(dbBak, new File(AppPaths.RUNTIME_DB));
                        System.out.println("  [✓] Restored database to " + AppPaths.RUNTIME_DB);
                    }
                } finally {
                    if (reconnectNeeded) {
                        storage.reconnectDatabase();
                    }
                }
                inventoryService.reload();
                salesService.reload();
                System.out.println("  [✓] Backup set restoration complete from " + chosen.getName());
            } else {
                File target = new File(AppPaths.RUNTIME_CARS);
                if (chosen.getName().contains("sales")) {
                    target = new File(AppPaths.RUNTIME_SALES);
                } else if (chosen.getName().contains("db")) {
                    target = new File(AppPaths.RUNTIME_DB);
                }
                boolean isDb = target.getName().contains("db");
                boolean reconnectNeeded = false;
                try {
                    if (isDb && storage != null && storage.isJdbcActive()) {
                        storage.closeDatabaseConnection();
                        reconnectNeeded = true;
                    }
                    dataToolsService.restoreFile(chosen, target);
                } finally {
                    if (reconnectNeeded) {
                        storage.reconnectDatabase();
                    }
                }
                inventoryService.reload();
                salesService.reload();
                System.out.println("  [✓] Restore complete from " + chosen.getName() + " -> " + target.getName());
            }
        } catch (Exception e) {
            System.out.println("  [!] Restore failed: " + e.getMessage());
        }
    }
}
