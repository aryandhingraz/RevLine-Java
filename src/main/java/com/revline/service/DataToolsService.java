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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
     * 9 comma-separated fields: id,brand,model,price,topSpeed,fuelType,horsepower,acceleration,statusFlag
     * statusFlag: 1 = AVAILABLE, 0 = SOLD.
     */
    public LegacyImportResult importLegacyC(File legacyFile, InventoryService inventoryService) throws IOException {
        if (legacyFile == null || !legacyFile.exists()) {
            throw new IllegalArgumentException("Legacy file does not exist: " + legacyFile);
        }

        int imported = 0;
        int skipped = 0;
        List<Car> importedCars = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(legacyFile))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                String[] parts = trimmed.split(",", -1);
                if (parts.length < 9) {
                    skipped++;
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String brand = parts[1].trim();
                    String model = parts[2].trim();
                    double price = Double.parseDouble(parts[3].trim());
                    int topSpeed = Integer.parseInt(parts[4].trim());
                    FuelType fuelType = FuelType.fromString(parts[5].trim());
                    int horsepower = Integer.parseInt(parts[6].trim());
                    double acceleration = Double.parseDouble(parts[7].trim());
                    int statusInt = Integer.parseInt(parts[8].trim());
                    if (statusInt != 0 && statusInt != 1) {
                        throw new IllegalArgumentException("Legacy status flag must be 0 or 1: " + statusInt);
                    }
                    Status status = (statusInt == 1) ? Status.AVAILABLE : Status.SOLD;

                    Car car = new Car(id, brand, model, price, topSpeed, fuelType, horsepower, acceleration, status);

                    if (inventoryService != null) {
                        try {
                            inventoryService.addCar(car);
                            imported++;
                            importedCars.add(car);
                        } catch (Exception duplicateOrError) {
                            skipped++;
                        }
                    } else {
                        imported++;
                        importedCars.add(car);
                    }
                } catch (Exception parseError) {
                    skipped++;
                }
            }
        }

        if (logger != null) {
            logger.log("DATA_TOOLS", "LEGACY_IMPORT",
                    String.format("Imported %d cars, skipped %d from %s", imported, skipped, legacyFile.getName()));
        }

        return new LegacyImportResult(imported, skipped, importedCars);
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
     * Imports vehicles from a CSV file, validating every row and writing error reports for invalid entries.
     * Demonstrates multi-catch exception handling (L3, L26).
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

                // Skip header line
                if (lineNumber == 1 && (trimmed.toLowerCase().startsWith("brand") || trimmed.toLowerCase().startsWith("id"))) {
                    continue;
                }

                String[] parts = trimmed.split(",", -1);
                if (parts.length < 8) {
                    rejected++;
                    errorLines.add(String.format("Line %3d: Rejected -> Expected >= 8 columns, found %d: [%s]",
                            lineNumber, parts.length, trimmed));
                    continue;
                }

                try {
                    // Support both 8-column (no id) and 9-column (with id) formats
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
                    if (inventoryService != null) {
                        inventoryService.addCar(car);
                    }
                    success++;

                } catch (NumberFormatException | InvalidCarException e) {
                    // Multi-catch handling parsing and validation failures (L3, §11.7)
                    rejected++;
                    errorLines.add(String.format("Line %3d: Rejected (%s) -> [%s]",
                            lineNumber, e.getMessage(), trimmed));
                } catch (Exception genericErr) {
                    rejected++;
                    errorLines.add(String.format("Line %3d: Rejected -> %s: [%s]",
                            lineNumber, genericErr.getMessage(), trimmed));
                }
            }
        }

        File errorReportFile = null;
        if (!errorLines.isEmpty() && errorDir != null) {
            if (!errorDir.exists()) errorDir.mkdirs();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            errorReportFile = new File(errorDir, "import_errors_" + timestamp + ".txt");
            List<String> reportContent = new ArrayList<>();
            reportContent.add("==========================================================================");
            reportContent.add("                 REVLINE CSV IMPORT VALIDATION ERROR REPORT               ");
            reportContent.add("==========================================================================");
            reportContent.add("Source File : " + csvFile.getAbsolutePath());
            reportContent.add(String.format("Statistics  : %d accepted, %d rejected%n", success, rejected));
            reportContent.add("--------------------------------------------------------------------------");
            reportContent.addAll(errorLines);
            reportContent.add("==========================================================================");
            try {
                FileUtil.safeWrite(errorReportFile, reportContent);
            } catch (RepositoryException ignored) {}
        }

        return new CsvImportResult(success, rejected, errorReportFile);
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
     * Configured with quoteChar('"'), eolIsSignificant(true), commentChar('#').
     */
    public ScriptImportResult importScript(File scriptFile, InventoryService inventoryService, SalesService salesService)
            throws IOException {
        if (scriptFile == null || !scriptFile.exists()) {
            throw new IllegalArgumentException("Script file does not exist: " + scriptFile);
        }

        int executed = 0;
        int failed = 0;
        List<String> log = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(scriptFile))) {
            StreamTokenizer st = new StreamTokenizer(br);
            st.resetSyntax();
            st.whitespaceChars(0, ' ');
            st.wordChars('a', 'z');
            st.wordChars('A', 'Z');
            st.wordChars('0', '9');
            st.wordChars('_', '_');
            st.wordChars('-', '-');
            st.wordChars('.', '.');
            st.quoteChar('"');
            st.commentChar('#');
            st.eolIsSignificant(true);

            int token;
            while ((token = st.nextToken()) != StreamTokenizer.TT_EOF) {
                if (token == StreamTokenizer.TT_EOL) {
                    continue;
                }

                if (token == StreamTokenizer.TT_WORD) {
                    String command = st.sval;
                    if (command.equalsIgnoreCase("ADD")) {
                        try {
                            st.nextToken(); String brand = st.sval;
                            st.nextToken(); String model = st.sval;
                            st.nextToken(); double price = Double.parseDouble(st.sval);
                            st.nextToken(); int speed = Integer.parseInt(st.sval);
                            st.nextToken(); FuelType fuel = FuelType.fromString(st.sval);
                            st.nextToken(); int hp = Integer.parseInt(st.sval);
                            st.nextToken(); double accel = Double.parseDouble(st.sval);

                            Car car = new Car(1, brand, model, price, speed, fuel, hp, accel, Status.AVAILABLE);
                            if (inventoryService != null) {
                                inventoryService.addCar(car);
                            }
                            executed++;
                            log.add("Executed ADD: " + brand + " " + model);
                        } catch (Exception e) {
                            failed++;
                            log.add("Failed ADD on line " + st.lineno() + ": " + e.getMessage());
                            skipToEol(st);
                        }
                    } else if (command.equalsIgnoreCase("SELL")) {
                        try {
                            st.nextToken(); int carId = Integer.parseInt(st.sval);
                            st.nextToken(); String buyer = st.sval;
                            if (salesService != null) {
                                salesService.sellCar(carId, buyer, "ScriptAgent");
                            }
                            executed++;
                            log.add("Executed SELL: Car #" + carId + " to " + buyer);
                        } catch (Exception e) {
                            failed++;
                            log.add("Failed SELL on line " + st.lineno() + ": " + e.getMessage());
                            skipToEol(st);
                        }
                    } else if (command.equalsIgnoreCase("RESERVE")) {
                        try {
                            st.nextToken(); int carId = Integer.parseInt(st.sval);
                            st.nextToken(); String buyer = st.sval;
                            st.nextToken(); int minutes = Integer.parseInt(st.sval);
                            if (salesService != null) {
                                salesService.reserveCar(carId, buyer, minutes);
                            }
                            executed++;
                            log.add("Executed RESERVE: Car #" + carId + " for " + buyer + " (" + minutes + " min)");
                        } catch (Exception e) {
                            failed++;
                            log.add("Failed RESERVE on line " + st.lineno() + ": " + e.getMessage());
                            skipToEol(st);
                        }
                    } else {
                        failed++;
                        log.add("Unknown command '" + command + "' on line " + st.lineno());
                        skipToEol(st);
                    }
                }
            }
        }

        return new ScriptImportResult(executed, failed, log);
    }

    private static void skipToEol(StreamTokenizer st) throws IOException {
        while (st.nextToken() != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF) {
            // consume remaining tokens on line
        }
    }

    // =========================================================================
    // 4. BYTE-STREAM BACKUP & RESTORE (L25)
    // =========================================================================

    /**
     * Executes a binary backup using FileInputStream / FileOutputStream with a 4KB buffer (L25).
     *
     * @param sourceFile source database or data file
     * @param backupFile target backup destination
     * @throws IOException on I/O failure
     */
    public void backupFile(File sourceFile, File backupFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IllegalArgumentException("Source file does not exist: " + sourceFile);
        }
        File parent = backupFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(backupFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        }

        if (logger != null) {
            logger.log("DATA_TOOLS", "BACKUP", "Created byte backup: " + backupFile.getName());
        }
    }

    /**
     * Restores a binary backup file using byte stream copy (L25).
     */
    public void restoreFile(File backupFile, File targetFile) throws IOException {
        if (backupFile == null || !backupFile.exists()) {
            throw new IllegalArgumentException("Backup file does not exist: " + backupFile);
        }
        backupFile(backupFile, targetFile); // reuses byte-copy loop
        if (logger != null) {
            logger.log("DATA_TOOLS", "RESTORE", "Restored file from: " + backupFile.getName());
        }
    }

    /**
     * Compares two files byte-by-byte to verify exact identity (L25).
     */
    public boolean areFilesByteIdentical(File file1, File file2) throws IOException {
        if (file1 == null || file2 == null || !file1.exists() || !file2.exists()) {
            return false;
        }
        if (file1.length() != file2.length()) {
            return false;
        }

        try (FileInputStream fis1 = new FileInputStream(file1);
             FileInputStream fis2 = new FileInputStream(file2)) {
            int b1, b2;
            while ((b1 = fis1.read()) != -1) {
                b2 = fis2.read();
                if (b1 != b2) return false;
            }
            return fis2.read() == -1;
        }
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

    private static final String[] SYNTHETIC_BRANDS = {
            "Apex", "Veloce", "Aero", "Hyperion", "Titan", "Phantom", "Stratos", "Zephyr"
    };

    private static final String[] SYNTHETIC_MODELS = {
            "GT-1", "Valkyrie-X", "Evo", "Spyder", "Prototipo", "Stradale", "Corsa", "Ultima"
    };

    /**
     * Generates realistic synthetic supercar data for benchmarking and stress testing.
     * All cars are explicitly labelled and maintain strictly valid specifications.
     */
    public List<Car> generateSyntheticCars(int count) {
        if (count <= 0) return new ArrayList<>();

        List<Car> generated = new ArrayList<>(count);
        Random rng = new Random(42); // fixed seed for deterministic reproducibility

        for (int i = 1; i <= count; i++) {
            String brand = SYNTHETIC_BRANDS[rng.nextInt(SYNTHETIC_BRANDS.length)];
            String model = SYNTHETIC_MODELS[rng.nextInt(SYNTHETIC_MODELS.length)] + " [SYN-" + i + "]";
            double price = 100_000.0 + (rng.nextInt(290) * 10_000.0);
            int speed = 250 + rng.nextInt(200); // 250 - 450 km/h
            FuelType fuel = FuelType.values()[rng.nextInt(FuelType.values().length)];
            int hp = 500 + rng.nextInt(1500); // 500 - 2000 HP
            double accel = Math.round((1.8 + (rng.nextDouble() * 2.5)) * 100.0) / 100.0; // 1.80 - 4.30s

            generated.add(new Car(1000 + i, brand, model, price, speed, fuel, hp, accel, Status.AVAILABLE));
        }

        return generated;
    }
}
