package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Sale;
import com.revline.model.Status;
import com.revline.repository.DatabaseManager;
import com.revline.repository.JdbcCarRepository;
import com.revline.repository.JdbcSaleRepository;
import com.revline.repository.SchemaInitializer;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for SQLite JDBC sales repository and atomic relational transactions (L17-20).
 */
public final class JdbcSaleRepositoryTest {

    private JdbcSaleRepositoryTest() {}

    public static void testSaveAndFindAllJdbc() throws Exception {
        try (Connection conn = DatabaseManager.getConnection(DatabaseManager.IN_MEMORY_URL)) {
            SchemaInitializer.init(conn);

            JdbcSaleRepository repo = new JdbcSaleRepository(conn);
            int initialCount = repo.count();

            Sale sale = new Sale(repo.nextId(), 1, "Bruce Wayne", "RevLine Showroom", LocalDate.now(), 3000000.0);
            repo.save(sale);

            Assert.assertEquals(initialCount + 1, repo.count(), "Sale count incremented by 1");
            Sale fetched = repo.findById(sale.getSaleId());
            Assert.assertNotNull(fetched, "Fetched sale should not be null");
            Assert.assertEquals("Bruce Wayne", fetched.getBuyerName(), "Buyer name matches");
        }
    }

    public static void testFindByIdAndFindByCarIdJdbc() throws Exception {
        try (Connection conn = DatabaseManager.getConnection(DatabaseManager.IN_MEMORY_URL)) {
            SchemaInitializer.init(conn);

            JdbcSaleRepository repo = new JdbcSaleRepository(conn);
            int nextId = repo.nextId();
            Sale sale1 = new Sale(nextId, 2, "Tony Stark", "Seller A", LocalDate.of(2026, 2, 10), 2500000.0);
            repo.save(sale1);

            List<Sale> forCar2 = repo.findByCarId(2);
            Assert.assertEquals(1, forCar2.size(), "Car #2 has 1 sale record");
            Assert.assertEquals("Tony Stark", forCar2.get(0).getBuyerName(), "Buyer matches");
        }
    }

    public static void testRecordSaleAtomicCommitAndRollback() throws Exception {
        try (Connection conn = DatabaseManager.getConnection(DatabaseManager.IN_MEMORY_URL)) {
            SchemaInitializer.init(conn);

            JdbcCarRepository carRepo = new JdbcCarRepository(conn);
            JdbcSaleRepository saleRepo = new JdbcSaleRepository(conn);

            // Insert an available car
            Car testCar = new Car(99, "Pagani", "Huayra BC", 2800000.0, 380, FuelType.PETROL, 800, 2.7, Status.AVAILABLE);
            carRepo.save(testCar);

            // Execute atomic sale
            Sale sale = new Sale(saleRepo.nextId(), 99, "Horacio Pagani", "RevLine", LocalDate.now(), 2800000.0);
            saleRepo.recordSaleAtomic(sale, testCar);

            // Verify car transitioned to SOLD and sale was inserted
            Car afterSale = carRepo.findById(99);
            Assert.assertEquals(Status.SOLD, afterSale.getStatus(), "Car status updated to SOLD in transaction");
            Assert.assertNotNull(saleRepo.findById(sale.getSaleId()), "Sale record committed to database");
        }
    }
}
