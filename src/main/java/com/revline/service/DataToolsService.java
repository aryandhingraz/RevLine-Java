package com.revline.service;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.repository.CarRepository;
import com.revline.util.AppLogger;
import com.revline.util.FileUtil;
import com.revline.util.Validator;
import com.revline.util.exception.InvalidCarException;
import com.revline.util.exception.RepositoryException;
import com.revline.util.exception.RevLineException;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Enterprise Data Tools for file handling, migrations, batch scripts, and byte-stream backups (L25–L27).
 */
public class DataToolsService {

    private final AppLogger logger;

    public DataToolsService(AppLogger logger) {
        this.logger = logger;
    }

    // =========================================================================
    // 1. LEGACY C IMPORTER (L26, L27)
    // =========================================================================

    public static class LegacyImportResult {
        private final int importedCount;
        private final int skippedCount;
        private final List<Car> importedCars;

        public LegacyImportResult(int importedCount, int skippedCount, List<Car> importedCars) {
            this.importedCount = importedCount;
            this.skippedCount = skippedCount;
            this.importedCars = importedCars;
        }

        public int getImportedCount() { return importedCount; }
        public int getSkippedCount() { return skippedCount; }
        public List<Car> getImportedCars() { return importedCars; }

        @Override
        public String toString() {
            return String.format("Legacy Import Summary: %d imported, %d skipped", importedCount, skippedCount);
        }
    }

    /**
     * Imports vehicles from the legacy C flat-file format (cars.txt).
     */
    public LegacyImportResult importLegacyC(File legacyFile, InventoryService inventoryService) throws IOException {
        return LegacyCImporter.importFile(legacyFile, inventoryService, logger);
    }

    // =========================================================================
    // 2. CSV BATCH IMPORTER WITH ERROR LOGGING (L26, L27, L3)
    // =========================================================================

    public static class CsvImportResult {
        private final int successCount;
        private final int rejectedCount;
        private final File errorReportFile;

        public CsvImportResult(int successCount, int rejectedCount, File errorReportFile) {
            this.successCount = successCount;
            this.rejectedCount = rejectedCount;
            this.errorReportFile = errorReportFile;
        }

        public int getSuccessCount() { return successCount; }
        public int getRejectedCount() { return rejectedCount; }
        public File getErrorReportFile() { return errorReportFile; }

        @Override
        public String toString() {
            String errInfo = (errorReportFile != null) ? " (Report: " + errorReportFile.getName() + ")" : "";
            return String.format("CSV Import Summary: %d accepted, %d rejected%s", successCount, rejectedCount, errInfo);
        }
    }

    /**
     * Imports vehicles from a CSV file, validating every row and writing error reports for invalid entries (L3, L26).
     */
    public CsvImportResult importCsv(File csvFile, InventoryService inventoryService, File errorDir) throws IOException {
        if (csvFile == null || !csvFile.exists()) {
            throw new IllegalArgumentException("CSV file does not exist: " + csvFile);
        }
        int success = 0;
        int rejected = 0;
        List<String> errorLines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (lineNumber == 1 && (trimmed.toLowerCase().startsWith("brand") || trimmed.toLowerCase().startsWith("id"))) {
                    continue;
                }
                String[] parts = trimmed.split(",", -1);
                if (parts.length < 8) {
                    rejected++;
                    errorLines.add(String.format("Line %3d: Rejected -> Expected >= 8 columns, found %d: [%s]", lineNumber, parts.length, trimmed));
                    continue;
                }
                try {
                    int offset = (parts.length >= 9) ? 1 : 0;
                    String brand = parts[offset].trim();
                    String model = parts[offset + 1].trim();
                    double price = Double.parseDouble(parts[offset + 2].trim());
                    int topSpeed = Integer.parseInt(parts[offset + 3].trim());
                    FuelType fuelType = FuelType.fromString(parts[offset + 4].trim());
                    int horsepower = Integer.parseInt(parts[offset + 5].trim());
                    double acceleration = Double.parseDouble(parts[offset + 6].trim());
                    Status status = Status.fromString(parts[offset + 7].trim());
                    Car car = new Car(1, brand, model, price, topSpeed, fuelType, horsepower, acceleration, status);
                    if (inventoryService != null) { inventoryService.addCar(car); }
                    success++;
                } catch (NumberFormatException | InvalidCarException e) {
                    rejected++;
                    errorLines.add(String.format("Line %3d: Rejected (%s) -> [%s]", lineNumber, e.getMessage(), trimmed));
                } catch (Exception genericErr) {
                    rejected++;
                    errorLines.add(String.format("Line %3d: Rejected -> %s: [%s]", lineNumber, genericErr.getMessage(), trimmed));
                }
            }
        }

        File reportFile = writeErrorReport(csvFile, errorDir, success, rejected, errorLines);
        return new CsvImportResult(success, rejected, reportFile);
    }

    private File writeErrorReport(File csvFile, File errorDir, int success, int rejected, List<String> errorLines) {
        if (errorLines.isEmpty() || errorDir == null) return null;
        if (!errorDir.exists()) errorDir.mkdirs();
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File report = new File(errorDir, "import_errors_" + ts + ".txt");
        List<String> content = new ArrayList<>();
        content.add("==========================================================================");
        content.add("                 REVLINE CSV IMPORT VALIDATION ERROR REPORT               ");
        content.add("==========================================================================");
        content.add("Source File : " + csvFile.getAbsolutePath());
        content.add(String.format("Statistics  : %d accepted, %d rejected%n", success, rejected));
        content.add("--------------------------------------------------------------------------");
        content.addAll(errorLines);
        content.add("==========================================================================");
        try { FileUtil.safeWrite(report, content); } catch (RepositoryException ignored) {}
        return report;
    }

    // =========================================================================
    // 3. SCRIPT IMPORTER VIA StreamTokenizer (L25)
    // =========================================================================

    public static class ScriptImportResult {
        private final int executedCommands;
        private final int failedCommands;
        private final List<String> executionLog;

        public ScriptImportResult(int executedCommands, int failedCommands, List<String> executionLog) {
            this.executedCommands = executedCommands;
            this.failedCommands = failedCommands;
            this.executionLog = executionLog;
        }

        public int getExecutedCommands() { return executedCommands; }
        public int getFailedCommands() { return failedCommands; }
        public List<String> getExecutionLog() { return executionLog; }
    }

    /**
     * Parses and executes a batch DSL script file using java.io.StreamTokenizer (L25).
     */
    public ScriptImportResult importScript(File scriptFile, InventoryService inventoryService, SalesService salesService)
            throws IOException {
        return ScriptRunner.run(scriptFile, inventoryService, salesService);
    }

    // =========================================================================
    // 4. BYTE-STREAM BACKUP & RESTORE (L25)
    // =========================================================================

    /**
     * Executes a binary backup using FileInputStream / FileOutputStream with a 4KB buffer (L25).
     */
    public void backupFile(File sourceFile, File backupFile) throws IOException {
        ByteStreamBackup.backupFile(sourceFile, backupFile, logger);
    }

    /**
     * Restores a binary backup file using byte stream copy (L25).
     */
    public void restoreFile(File backupFile, File targetFile) throws IOException {
        ByteStreamBackup.restoreFile(backupFile, targetFile, logger);
    }

    /**
     * Compares two files byte-by-byte to verify exact identity (L25).
     */
    public boolean areFilesByteIdentical(File file1, File file2) throws IOException {
        return ByteStreamBackup.areFilesByteIdentical(file1, file2);
    }

    // =========================================================================
    // 5. STORAGE MIGRATION TOOL (L24 Generics, L17-L20)
    // =========================================================================

    /**
     * Migrates all vehicles from one repository implementation to another (e.g. CSV to SQLite).
     * Demonstrates generic repository interoperability (L24).
     */
    public int migrate(CarRepository from, CarRepository to) throws RepositoryException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Repositories cannot be null.");
        }
        List<Car> allCars = from.findAll();
        for (Car car : allCars) {
            to.save(car);
        }
        if (logger != null) {
            logger.log("DATA_TOOLS", "MIGRATE", "Migrated " + allCars.size() + " cars across repositories.");
        }
        return allCars.size();
    }

    // =========================================================================
    // 6. SYNTHETIC SUPERCAR DATA GENERATOR (L4)
    // =========================================================================

    /**
     * Generates realistic synthetic supercar data for benchmarking and stress testing.
     */
    public List<Car> generateSyntheticCars(int count) {
        return SyntheticDataGenerator.generate(count);
    }
}
