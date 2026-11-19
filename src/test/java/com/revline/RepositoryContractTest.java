package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Sale;
import com.revline.model.Status;
import com.revline.repository.CarRepository;
import com.revline.repository.DatabaseManager;
import com.revline.repository.FileCarRepository;
import com.revline.repository.FileSaleRepository;
import com.revline.repository.JdbcCarRepository;
import com.revline.repository.JdbcSaleRepository;
import com.revline.repository.SaleRepository;
import com.revline.repository.SchemaInitializer;
import com.revline.util.FileUtil;

import java.io.File;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Parametric contract test suite verifying identical behavior across both
 * CSV flat-file and SQLite JDBC repository implementations (GATE 4 Requirement).
 */
public final class RepositoryContractTest {

    private RepositoryContractTest() {}

    public static void testCarRepositoryContractInBothModes() throws Exception {
        // Mode 1: CSV Flat-File
        File tempCsv = new File("build/test_contract_cars.csv");
        if (tempCsv.exists()) tempCsv.delete();
        FileUtil.safeWrite(tempCsv, Collections.singletonList(FileCarRepository.CSV_HEADER));
        CarRepository csvRepo = new FileCarRepository(tempCsv);
        verifyCarRepositoryContract("CSV Mode", csvRepo);
        tempCsv.delete();

        // Mode 2: SQLite JDBC
        try (Connection conn = DatabaseManager.getConnection(DatabaseManager.IN_MEMORY_URL)) {
            SchemaInitializer.init(conn);
            CarRepository jdbcRepo = new JdbcCarRepository(conn);
            verifyCarRepositoryContract("JDBC Mode", jdbcRepo);
        }
    }

    public static void testSaleRepositoryContractInBothModes() throws Exception {
        // Mode 1: CSV Flat-File
        File tempCsv = new File("build/test_contract_sales.csv");
        if (tempCsv.exists()) tempCsv.delete();
        FileUtil.safeWrite(tempCsv, Collections.singletonList(FileSaleRepository.CSV_HEADER));
        SaleRepository csvSaleRepo = new FileSaleRepository(tempCsv);
        verifySaleRepositoryContract("CSV Mode", csvSaleRepo);
        tempCsv.delete();

        // Mode 2: SQLite JDBC
        try (Connection conn = DatabaseManager.getConnection(DatabaseManager.IN_MEMORY_URL)) {
            SchemaInitializer.init(conn);
            SaleRepository jdbcSaleRepo = new JdbcSaleRepository(conn);
            verifySaleRepositoryContract("JDBC Mode", jdbcSaleRepo);
        }
    }

    private static void verifyCarRepositoryContract(String mode, CarRepository repo) throws Exception {
        int initial = repo.count();

        // 1. Next ID and Save
        int id = repo.nextId();
        Car car = new Car(id, "Zenvo", "TSR-S", 1800000.0, 325, FuelType.PETROL, 1177, 2.8, Status.AVAILABLE);
        repo.save(car);

        Assert.assertEquals(initial + 1, repo.count(), mode + ": count increases by 1 after save");

        // 2. Find by ID
        Car found = repo.findById(id);
        Assert.assertNotNull(found, mode + ": car found by ID");
        Assert.assertEquals("Zenvo", found.getBrand(), mode + ": brand matches");

        // 3. Find by Status
        List<Car> availableCars = repo.findByStatus(Status.AVAILABLE);
        Assert.assertTrue(availableCars.stream().anyMatch(c -> c.getId() == id),
                mode + ": newly added available car present in findByStatus");

        // 4. Find by Price Range (EX-6 contract)
        List<Car> inRange = repo.findByPriceRange(1500000.0, 2000000.0);
        Assert.assertTrue(inRange.stream().anyMatch(c -> c.getId() == id),
                mode + ": newly added car present in price range [1.5M, 2.0M]");

        // 5. Delete by ID
        boolean deleted = repo.deleteById(id);
        Assert.assertTrue(deleted, mode + ": deleteById returns true");
        Assert.assertNull(repo.findById(id), mode + ": car absent after deletion");
        Assert.assertEquals(initial, repo.count(), mode + ": count returns to initial");
    }

    private static void verifySaleRepositoryContract(String mode, SaleRepository repo) throws Exception {
        int initial = repo.count();

        int saleId = repo.nextId();
        Sale sale = new Sale(saleId, 1, "Buyer Test", "Seller Test", LocalDate.now(), 500000.0);
        repo.save(sale);

        Assert.assertEquals(initial + 1, repo.count(), mode + ": sale count increases by 1");

        Sale found = repo.findById(saleId);
        Assert.assertNotNull(found, mode + ": sale found by ID");
        Assert.assertEquals("Buyer Test", found.getBuyerName(), mode + ": buyer name matches");

        List<Sale> forCar1 = repo.findByCarId(1);
        Assert.assertTrue(forCar1.stream().anyMatch(s -> s.getSaleId() == saleId),
                mode + ": sale found by car ID");

        boolean deleted = repo.deleteById(saleId);
        Assert.assertTrue(deleted, mode + ": sale deleted");
        Assert.assertEquals(initial, repo.count(), mode + ": count returns to initial");
    }
}
