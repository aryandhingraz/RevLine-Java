package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.repository.DatabaseManager;
import com.revline.repository.JdbcCarRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Proof-of-concept test demonstrating SQL Injection immunity via PreparedStatement (L19).
 */
public final class SqlInjectionTest {

    private SqlInjectionTest() {}

    /**
     * Demonstrates that passing classic SQL injection payloads like "' OR '1'='1"
     * into a PreparedStatement treats the input strictly as literal data, preventing injection.
     */
    public static void testPreparedStatementImmunityToSqlInjection() throws Exception {
        try (Connection connection = DatabaseManager.getConnection(DatabaseManager.IN_MEMORY_URL)) {
            // Setup test schema & seed cars
            try (Statement s = connection.createStatement()) {
                s.execute("CREATE TABLE cars ("
                        + "car_id INTEGER PRIMARY KEY, brand TEXT, model TEXT, price REAL, "
                        + "top_speed INTEGER, fuel_type TEXT, horsepower INTEGER, acceleration REAL, status TEXT)");
                s.execute("INSERT INTO cars VALUES (1, 'Ferrari', 'Roma', 250000.0, 320, 'PETROL', 620, 3.4, 'AVAILABLE')");
                s.execute("INSERT INTO cars VALUES (2, 'Bugatti', 'Chiron', 3000000.0, 420, 'PETROL', 1500, 2.4, 'AVAILABLE')");
            }

            // Attacker payload attempting to bypass filter and dump entire table
            String attackPayload = "' OR '1'='1";

            // 1. SECURE PATH (PreparedStatement with parameter binding)
            String secureSql = "SELECT * FROM cars WHERE brand = ?";
            int secureMatchCount = 0;
            try (PreparedStatement ps = connection.prepareStatement(secureSql)) {
                ps.setString(1, attackPayload);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        secureMatchCount++;
                    }
                }
            }
            Assert.assertEquals(0, secureMatchCount,
                    "PreparedStatement binds attack payload as a literal string; matches 0 cars");

            // 2. INSECURE DEMONSTRATION (What would have happened with raw string concatenation)
            String insecureSql = "SELECT * FROM cars WHERE brand = '" + attackPayload + "'";
            int insecureMatchCount = 0;
            try (Statement rawStatement = connection.createStatement();
                 ResultSet rs = rawStatement.executeQuery(insecureSql)) {
                while (rs.next()) {
                    insecureMatchCount++;
                }
            }
            Assert.assertEquals(2, insecureMatchCount,
                    "Raw concatenation falls victim to SQL injection; dumps all 2 cars");

            // 3. SECURE REPOSITORY PATH
            JdbcCarRepository repository = new JdbcCarRepository(connection);
            Car injectedCar = repository.findById(1);
            Assert.assertNotNull(injectedCar, "Standard query succeeds");

            // Ensure table was not dropped or altered
            Assert.assertEquals(2, repository.count(), "All records intact; injection attack neutralized");
        }
    }
}
