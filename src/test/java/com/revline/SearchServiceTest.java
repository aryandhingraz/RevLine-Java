package com.revline;

import com.revline.model.Car;
import com.revline.model.Status;
import com.revline.repository.FileCarRepository;
import com.revline.service.InventoryService;
import com.revline.service.SearchService;
import com.revline.util.exception.CarNotFoundException;
import com.revline.util.exception.RepositoryException;

import java.io.File;
import java.util.List;

/**
 * Tests for SearchService discovery and student exercise EX-3 (L2, L5).
 */
public class SearchServiceTest {

    private static SearchService createSeedSearchService() throws RepositoryException {
        File seedFile = new File("data/seed/cars.csv");
        FileCarRepository repo = new FileCarRepository(seedFile);
        InventoryService inv = new InventoryService(repo, null);
        return new SearchService(inv);
    }

    public static void testFindByIdDelegation() throws RepositoryException, CarNotFoundException {
        SearchService search = createSeedSearchService();
        Car c = search.findById(1);
        Assert.assertNotNull(c, "Car #1 should be found");
        Assert.assertEquals("Bugatti", c.getBrand(), "Brand must match");
    }

    public static void testFindByStatus() throws RepositoryException {
        SearchService search = createSeedSearchService();
        List<Car> available = search.findByStatus(Status.AVAILABLE);
        List<Car> sold = search.findByStatus(Status.SOLD);

        Assert.assertEquals(15, available.size(), "Should find 15 available cars");
        Assert.assertEquals(3, sold.size(), "Should find 3 sold cars");
    }

    // =========================================================================
    // STUDENT EXERCISE TESTS (EX-3)
    // =========================================================================

    public static void testExercise3_searchMultiField() throws RepositoryException {
        SearchService search = createSeedSearchService();

        // 1. Single word brand search (case-insensitive)
        List<Car> ferraris = search.search("ferrari");
        Assert.assertEquals(2, ferraris.size(), "Should find 2 Ferraris (LaFerrari, SF90)");

        // 2. Fuel type search
        List<Car> electrics = search.search("ELECTRIC");
        Assert.assertEquals(1, electrics.size(), "Should find 1 electric supercar (Tesla)");

        // 3. Multi-token search (brand + model or brand + fuel)
        List<Car> hybridFerrari = search.search("Ferrari Hybrid");
        Assert.assertEquals(2, hybridFerrari.size(), "Both Ferraris are Hybrids");

        // 4. Non-matching search
        List<Car> nonExistent = search.search("Toyota Corolla");
        Assert.assertEquals(0, nonExistent.size(), "Unmatched query should return 0 results");
    }
}
