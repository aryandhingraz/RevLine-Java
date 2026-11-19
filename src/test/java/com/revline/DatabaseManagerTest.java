package com.revline;

import com.revline.repository.DatabaseManager;
import com.revline.repository.SchemaInitializer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Tests for DatabaseManager connection lifecycles and PRAGMA settings (L17-L18).
 */
public final class DatabaseManagerTest {

    private DatabaseManagerTest() {}

    public static void testConnectionAndPragmaForeignKeys() throws Exception {
        try (Connection conn = DatabaseManager.getConnection(DatabaseManager.IN_MEMORY_URL)) {
            Assert.assertNotNull(conn, "Connection should be established");
            Assert.assertFalse(conn.isClosed(), "Connection should be open");

            // Verify foreign_keys PRAGMA is enabled (1)
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("PRAGMA foreign_keys;")) {
                Assert.assertTrue(rs.next(), "Result set has row for foreign_keys");
                int fkEnabled = rs.getInt(1);
                Assert.assertEquals(1, fkEnabled, "PRAGMA foreign_keys must be enabled (1)");
            }
        }
    }

    public static void testSchemaInitializerCreatesTablesAndSeeds() throws Exception {
        try (Connection conn = DatabaseManager.getConnection(DatabaseManager.IN_MEMORY_URL)) {
            SchemaInitializer.init(conn);

            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM cars")) {
                Assert.assertTrue(rs.next(), "Result set has count");
                int count = rs.getInt(1);
                Assert.assertTrue(count >= 18, "Seeded cars table has at least 18 cars from seed CSV");
            }

            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM sales")) {
                Assert.assertTrue(rs.next(), "Result set has count");
                int count = rs.getInt(1);
                Assert.assertTrue(count >= 3, "Seeded sales table has at least 3 sales from seed CSV");
            }
        }
    }
}
