package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.repository.FileCarRepository;
import com.revline.service.InventoryService;
import com.revline.util.exception.CarNotFoundException;
import com.revline.util.exception.InvalidCarException;
import com.revline.util.exception.RepositoryException;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Tests for InventoryService business logic, collections, and defensive copying (L1, L5, L6).
 */
public class InventoryServiceTest {

    private static InventoryService createSeedService() throws RepositoryException {
        File seedFile = new File("data/seed/cars.csv");
        FileCarRepository repo = new FileCarRepository(seedFile);
        return new InventoryService(repo, null);
    }

    public static void testFindAllDefensiveCopyImmutability() throws RepositoryException {
        InventoryService service = createSeedService();
        List<Car> list = service.findAll();

        Assert.assertEquals(18, list.size(), "Should have 18 cars");

        // Attempting to mutate unmodifiable list should throw UnsupportedOperationException
        Assert.assertThrows(UnsupportedOperationException.class, () ->
            list.add(new Car(99, "Fake", "Car", 1000, 200, FuelType.PETROL, 300, 3.0, Status.AVAILABLE)),
            "External callers must not be able to modify the returned inventory list");
    }

    public static void testFindByIdExisting() throws RepositoryException, CarNotFoundException {
        InventoryService service = createSeedService();
        Car c = service.findById(1);
        Assert.assertEquals("Bugatti", c.getBrand(), "Car #1 is Bugatti");
        Assert.assertEquals("Chiron", c.getModel(), "Car #1 model is Chiron");
    }

    public static void testFindByIdThrowsWhenNotFound() throws RepositoryException {
        InventoryService service = createSeedService();
        Assert.assertThrows(CarNotFoundException.class, () ->
            service.findById(999), "Non-existent car ID must throw CarNotFoundException");
    }

    public static void testAddCarRejectsDuplicate() throws RepositoryException {
        InventoryService service = createSeedService();
        // Bugatti Chiron already exists in seed data
        Car dup = new Car(0, "Bugatti", "Chiron", 3500000.0, 420, FuelType.PETROL, 1500, 2.4, Status.AVAILABLE);

        Assert.assertThrows(InvalidCarException.class, () ->
            service.addCar(dup), "Duplicate brand and model must be rejected");
    }

    public static void testCalculateTotalStockValueOnlyAvailable() throws RepositoryException {
        InventoryService service = createSeedService();

        // 18 cars total, but IDs 3, 4, 18 are SOLD
        // Manually sum available prices from seed
        List<Car> all = service.findAll();
        double expectedStockValue = 0.0;
        int expectedAvailable = 0;
        for (Car c : all) {
            if (c.getStatus() == Status.AVAILABLE) {
                expectedStockValue += c.getPrice();
                expectedAvailable++;
            }
        }

        Assert.assertEquals(15, expectedAvailable, "15 cars must be AVAILABLE");
        Assert.assertEquals(expectedStockValue, service.calculateTotalStockValue(), 0.01,
                "Stock value must sum only AVAILABLE cars");
        Assert.assertTrue(service.calculateTotalPortfolioValue() > service.calculateTotalStockValue(),
                "Portfolio value (all cars) must be strictly greater than stock value (available only)");
    }

    public static void testGetBrandsSortedTreeSet() throws RepositoryException {
        InventoryService service = createSeedService();
        Set<String> brands = service.getBrandsSorted();

        Assert.assertTrue(brands.contains("Ferrari"), "Brands must contain Ferrari");
        Assert.assertTrue(brands.contains("Porsche"), "Brands must contain Porsche");

        // Verify natural alphabetical ordering
        String prev = null;
        for (String b : brands) {
            if (prev != null) {
                Assert.assertTrue(b.compareToIgnoreCase(prev) >= 0, "Brands must be sorted alphabetically");
            }
            prev = b;
        }
    }
}
