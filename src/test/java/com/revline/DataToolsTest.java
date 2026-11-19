package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.repository.FileCarRepository;
import com.revline.service.DataToolsService;
import com.revline.service.InventoryService;
import com.revline.service.ReservationManager;
import com.revline.service.SalesService;
import com.revline.service.WaitlistService;

import java.io.File;
import java.util.List;

/**
 * Gate 7 Test Suite: Enterprise Data Tools & File Handling (L25–L27).
 * Tests Legacy C import, CSV import with error report, StreamTokenizer scripts,
 * byte-stream backup/restore, lossless round-trip, and synthetic data generation.
 */
public final class DataToolsTest {

    private DataToolsTest() {}

    public static void testLegacyCImport() throws Exception {
        DataToolsService service = new DataToolsService(null);
        File legacyFile = new File("data/legacy/cars.txt");

        DataToolsService.LegacyImportResult result = service.importLegacyC(legacyFile, null);

        Assert.assertEquals(18, result.getImportedCount(), "Legacy C import should find exactly 18 cars");
        Assert.assertEquals(0, result.getSkippedCount(), "No legacy cars should be skipped");

        List<Car> cars = result.getImportedCars();
        // Verify sold car status flags (1=AVAILABLE, 0=SOLD)
        Assert.assertEquals(Status.AVAILABLE, cars.get(0).getStatus(), "Car #1 (Chiron) must be AVAILABLE");
        Assert.assertEquals(Status.AVAILABLE, cars.get(1).getStatus(), "Car #2 (Veyron SS) must be AVAILABLE");
        Assert.assertEquals(Status.SOLD, cars.get(2).getStatus(), "Car #3 (LaFerrari) must be SOLD");
        Assert.assertEquals(Status.SOLD, cars.get(3).getStatus(), "Car #4 (SF90) must be SOLD");
        Assert.assertEquals(Status.SOLD, cars.get(17).getStatus(), "Car #18 (R8 V10 Plus) must be SOLD");
    }

    public static void testCsvImportWithErrorReport() throws Exception {
        DataToolsService service = new DataToolsService(null);
        File sampleCsv = new File("data/sample/import_bad_rows.csv");
        File errorDir = new File("build/test_errors");

        DataToolsService.CsvImportResult result = service.importCsv(sampleCsv, null, errorDir);

        Assert.assertEquals(5, result.getSuccessCount(), "5 valid supercars should be accepted");
        Assert.assertEquals(4, result.getRejectedCount(), "4 malformed rows should be rejected");
        Assert.assertTrue(result.getErrorReportFile() != null && result.getErrorReportFile().exists(),
                "Error report file must be generated");

        // Verify error report file is non-empty
        Assert.assertTrue(result.getErrorReportFile().length() > 0, "Error report file should contain error logs");
    }

    public static void testStreamTokenizerScriptImport() throws Exception {
        DataToolsService service = new DataToolsService(null);
        File scriptFile = new File("data/sample/bulk_import.txt");

        File testCars = new File("build/test_script_cars.csv");
        File testSales = new File("build/test_script_sales.csv");
        if (testCars.exists()) testCars.delete();
        if (testSales.exists()) testSales.delete();

        FileCarRepository carRepo = new FileCarRepository(testCars);
        InventoryService inv = new InventoryService(carRepo, null);

        DataToolsService.ScriptImportResult result = service.importScript(scriptFile, inv, null);

        // 8 ADD commands executed (Ferrari, McLaren, Porsche, Rimac, Aston Martin, Lamborghini, Maserati, Mercedes)
        Assert.assertTrue(result.getExecutedCommands() >= 8, "Script tokenizer should execute all valid ADD commands");
        // 1 invalid command ("INVALID_CMD") caught and skipped
        Assert.assertTrue(result.getFailedCommands() >= 1, "Script tokenizer should detect and record syntax error");

        testCars.delete();
        testSales.delete();
    }

    public static void testByteStreamBackupRestoreByteIdentical() throws Exception {
        DataToolsService service = new DataToolsService(null);
        File source = new File("data/seed/cars.csv");
        File backup = new File("build/test_cars_backup.bak");
        File restored = new File("build/test_cars_restored.csv");

        if (backup.exists()) backup.delete();
        if (restored.exists()) restored.delete();

        // 1. Backup using byte stream
        service.backupFile(source, backup);
        Assert.assertTrue(backup.exists(), "Backup file must exist");
        Assert.assertEquals(source.length(), backup.length(), "Backup file size must match source file size");

        // 2. Restore using byte stream
        service.restoreFile(backup, restored);
        Assert.assertTrue(restored.exists(), "Restored file must exist");

        // 3. Byte-by-byte comparison
        boolean identical = service.areFilesByteIdentical(source, restored);
        Assert.assertTrue(identical, "Source and restored file must be 100% byte-identical");

        backup.delete();
        restored.delete();
    }

    public static void testLosslessRoundTripImportExportImport() throws Exception {
        DataToolsService service = new DataToolsService(null);

        // Source seed cars
        FileCarRepository repo1 = new FileCarRepository(new File("data/seed/cars.csv"));
        List<Car> originalCars = repo1.findAll();

        // Export to new file
        File roundTripFile = new File("build/test_roundtrip_cars.csv");
        if (roundTripFile.exists()) roundTripFile.delete();

        FileCarRepository repoExport = new FileCarRepository(roundTripFile);
        repoExport.saveAll(originalCars);

        // Import back from exported file
        FileCarRepository repo2 = new FileCarRepository(roundTripFile);
        List<Car> reloadedCars = repo2.findAll();

        Assert.assertEquals(originalCars.size(), reloadedCars.size(), "Round-trip car count must match");

        for (int i = 0; i < originalCars.size(); i++) {
            Car c1 = originalCars.get(i);
            Car c2 = reloadedCars.get(i);

            Assert.assertEquals(c1.getId(), c2.getId(), "Round-trip ID must match for index " + i);
            Assert.assertEquals(c1.getBrand(), c2.getBrand(), "Round-trip Brand must match");
            Assert.assertEquals(c1.getModel(), c2.getModel(), "Round-trip Model must match");
            Assert.assertEquals(c1.getPrice(), c2.getPrice(), 0.001, "Round-trip Price must match");
            Assert.assertEquals(c1.getTopSpeed(), c2.getTopSpeed(), "Round-trip TopSpeed must match");
            Assert.assertEquals(c1.getFuelType(), c2.getFuelType(), "Round-trip FuelType must match");
            Assert.assertEquals(c1.getHorsepower(), c2.getHorsepower(), "Round-trip Horsepower must match");
            Assert.assertEquals(c1.getAcceleration(), c2.getAcceleration(), 0.001, "Round-trip Acceleration must match");
            Assert.assertEquals(c1.getStatus(), c2.getStatus(), "Round-trip Status must match");
        }

        roundTripFile.delete();
    }

    public static void testSyntheticDataGenerator() {
        DataToolsService service = new DataToolsService(null);
        List<Car> synthetic = service.generateSyntheticCars(100);

        Assert.assertEquals(100, synthetic.size(), "Should generate exactly 100 synthetic cars");

        for (Car c : synthetic) {
            Assert.assertTrue(c.getPrice() >= 1000.0, "Synthetic car price must be valid");
            Assert.assertTrue(c.getTopSpeed() >= 50 && c.getTopSpeed() <= 600, "Synthetic speed within bounds");
            Assert.assertTrue(c.getHorsepower() >= 50 && c.getHorsepower() <= 3000, "Synthetic HP within bounds");
            Assert.assertTrue(c.getAcceleration() >= 1.0 && c.getAcceleration() <= 20.0, "Synthetic accel within bounds");
            Assert.assertEquals(Status.AVAILABLE, c.getStatus(), "Synthetic car status should be AVAILABLE");
        }
    }

    public static void testStorageMigrationCountsMatch() throws Exception {
        DataToolsService service = new DataToolsService(null);

        File f1 = new File("data/seed/cars.csv");
        FileCarRepository srcRepo = new FileCarRepository(f1);

        File f2 = new File("build/test_migrate_target.csv");
        if (f2.exists()) f2.delete();
        FileCarRepository targetRepo = new FileCarRepository(f2);

        int migrated = service.migrate(srcRepo, targetRepo);
        Assert.assertEquals(18, migrated, "Migration should transfer all 18 cars");
        Assert.assertEquals(18, targetRepo.findAll().size(), "Target repository count should be 18");

        f2.delete();
    }
}
