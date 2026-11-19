package com.revline;

import com.revline.model.Sale;
import com.revline.repository.FileSaleRepository;
import com.revline.util.FileUtil;
import com.revline.util.exception.RepositoryException;

import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Tests for CSV persistence and atomic safe-write of sales records (L26-27).
 */
public class FileSaleRepositoryTest {

    private static File getTempFile() {
        File file = new File("build/test_sales.csv");
        if (file.exists()) file.delete();
        return file;
    }

    public static void testSaveAndFindAll() throws RepositoryException {
        File file = getTempFile();
        FileUtil.safeWrite(file, Collections.singletonList(FileSaleRepository.CSV_HEADER));

        FileSaleRepository repo = new FileSaleRepository(file);
        Assert.assertEquals(0, repo.count(), "Initial repository is empty");

        Sale s1 = new Sale(1, 10, "Bruce Wayne", "Seller A", LocalDate.of(2026, 3, 1), 500000.0);
        Sale s2 = new Sale(2, 11, "Tony Stark", "Seller B", LocalDate.of(2026, 3, 2), 750000.0);

        repo.save(s1);
        repo.save(s2);

        Assert.assertEquals(2, repo.count(), "Repository holds 2 sales");
        List<Sale> all = repo.findAll();
        Assert.assertEquals(2, all.size(), "findAll returns 2 records");
        Assert.assertEquals("Bruce Wayne", all.get(0).getBuyerName(), "Buyer match");

        // Verify persistence from new repository instance
        FileSaleRepository reloaded = new FileSaleRepository(file);
        Assert.assertEquals(2, reloaded.count(), "Reloaded repo maintains persisted sales");

        file.delete();
    }

    public static void testFindByIdAndCarId() throws RepositoryException {
        File file = getTempFile();
        FileUtil.safeWrite(file, Collections.singletonList(FileSaleRepository.CSV_HEADER));

        FileSaleRepository repo = new FileSaleRepository(file);
        Sale s1 = new Sale(1, 10, "Buyer 1", "Seller", LocalDate.of(2026, 1, 1), 200000.0);
        Sale s2 = new Sale(2, 10, "Buyer 2", "Seller", LocalDate.of(2026, 2, 1), 220000.0);
        Sale s3 = new Sale(3, 15, "Buyer 3", "Seller", LocalDate.of(2026, 3, 1), 300000.0);

        repo.save(s1);
        repo.save(s2);
        repo.save(s3);

        Sale foundById = repo.findById(2);
        Assert.assertNotNull(foundById, "Should find sale with ID 2");
        Assert.assertEquals("Buyer 2", foundById.getBuyerName(), "Buyer name matches");

        List<Sale> byCarId = repo.findByCarId(10);
        Assert.assertEquals(2, byCarId.size(), "Car #10 has 2 sales records");

        file.delete();
    }

    public static void testNextIdGeneration() throws RepositoryException {
        File file = getTempFile();
        FileUtil.safeWrite(file, Collections.singletonList(FileSaleRepository.CSV_HEADER));

        FileSaleRepository repo = new FileSaleRepository(file);
        Assert.assertEquals(1, repo.nextId(), "First sale ID is 1");

        repo.save(new Sale(1, 5, "Buyer", "Seller", LocalDate.now(), 100000.0));
        Assert.assertEquals(2, repo.nextId(), "Next sale ID is 2");

        file.delete();
    }

    public static void testCrashSafeWriteIntegrity() throws RepositoryException {
        File file = getTempFile();
        FileUtil.safeWrite(file, Collections.singletonList(FileSaleRepository.CSV_HEADER));

        FileSaleRepository repo = new FileSaleRepository(file);
        repo.save(new Sale(1, 1, "Buyer", "Seller", LocalDate.now(), 150000.0));

        File bakFile = new File(file.getPath() + ".bak");
        File tmpFile = new File(file.getPath() + ".tmp");

        Assert.assertFalse(tmpFile.exists(), "Temporary write file must be cleanly deleted");
        Assert.assertFalse(bakFile.exists(), "Backup file must be cleanly deleted on clean completion");

        file.delete();
    }
}
