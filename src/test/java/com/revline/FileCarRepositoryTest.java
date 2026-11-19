package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.repository.FileCarRepository;
import com.revline.util.exception.RepositoryException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Tests for FileCarRepository file I/O and crash-safe operations (L25, L26).
 */
public class FileCarRepositoryTest {

    public static void testFindAllReturnsSeededCount() throws RepositoryException {
        File seedFile = new File("data/seed/cars.csv");
        FileCarRepository repo = new FileCarRepository(seedFile);
        Assert.assertEquals(18, repo.count(), "Repository should load 18 seed records");
        Assert.assertEquals(18, repo.findAll().size(), "findAll() size must be 18");
    }

    public static void testFindByIdExistingAndNonExisting() throws RepositoryException {
        File seedFile = new File("data/seed/cars.csv");
        FileCarRepository repo = new FileCarRepository(seedFile);

        Car car1 = repo.findById(1);
        Assert.assertNotNull(car1, "Car #1 should be found");
        Assert.assertEquals("Bugatti", car1.getBrand(), "Car #1 brand must be Bugatti");

        Car missing = repo.findById(999);
        Assert.assertEquals(null, missing, "Car #999 must return null");
    }

    public static void testFindByStatus() throws RepositoryException {
        File seedFile = new File("data/seed/cars.csv");
        FileCarRepository repo = new FileCarRepository(seedFile);

        List<Car> available = repo.findByStatus(Status.AVAILABLE);
        List<Car> sold = repo.findByStatus(Status.SOLD);

        Assert.assertEquals(15, available.size(), "There should be 15 AVAILABLE seed cars");
        Assert.assertEquals(3, sold.size(), "There should be 3 SOLD seed cars (IDs 3, 4, 18)");
    }

    public static void testFindByPriceRange() throws RepositoryException {
        File seedFile = new File("data/seed/cars.csv");
        FileCarRepository repo = new FileCarRepository(seedFile);

        // Cars between 100k and 200k: Tesla (135990), BMW (142000), Audi (197000)
        List<Car> matches = repo.findByPriceRange(100000.0, 200000.0);
        Assert.assertEquals(3, matches.size(), "Should find 3 cars in the 100k-200k range");
    }

    public static void testNextId() throws RepositoryException {
        File seedFile = new File("data/seed/cars.csv");
        FileCarRepository repo = new FileCarRepository(seedFile);
        Assert.assertEquals(19, repo.nextId(), "Next ID after 18 cars must be 19");
    }

    public static void testCrashSafeWriteIntegrity() throws IOException, RepositoryException {
        File temp = File.createTempFile("test_revline_", ".csv");
        temp.deleteOnExit();

        FileCarRepository tempRepo = new FileCarRepository(temp);
        Assert.assertEquals(0, tempRepo.count(), "Initial temp repo should be empty");

        Car c1 = new Car(1, "TestBrand", "M1", 50000, 250, FuelType.PETROL, 400, 3.5, Status.AVAILABLE);
        tempRepo.save(c1);

        Assert.assertEquals(1, tempRepo.count(), "Should have 1 car after save");

        // Verify file on disk reloads cleanly
        FileCarRepository reloaded = new FileCarRepository(temp);
        Assert.assertEquals(1, reloaded.count(), "Reloaded file should have 1 car");
        Assert.assertEquals("TestBrand", reloaded.findById(1).getBrand(), "Brand must match after disk write");

        temp.delete();
    }
}
