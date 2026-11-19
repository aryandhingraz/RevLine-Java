package com.revline.repository;

import com.revline.model.Car;
import com.revline.model.Sale;
import com.revline.util.exception.RepositoryException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes database relational schema and populates initial seed records (L17-L19).
 */
public final class SchemaInitializer {

    public static final String CREATE_CARS_TABLE =
            "CREATE TABLE IF NOT EXISTS cars ("
            + "  car_id        INTEGER PRIMARY KEY,"
            + "  brand         VARCHAR(50)   NOT NULL,"
            + "  model         VARCHAR(50)   NOT NULL,"
            + "  price         DECIMAL(12,2) NOT NULL CHECK (price > 0),"
            + "  top_speed     INTEGER       NOT NULL CHECK (top_speed BETWEEN 50 AND 600),"
            + "  fuel_type     VARCHAR(20)   NOT NULL CHECK (fuel_type IN ('PETROL','HYBRID','ELECTRIC')),"
            + "  horsepower    INTEGER       NOT NULL CHECK (horsepower BETWEEN 50 AND 3000),"
            + "  acceleration  DECIMAL(4,2)  NOT NULL CHECK (acceleration BETWEEN 1 AND 20),"
            + "  status        VARCHAR(10)   NOT NULL CHECK (status IN ('AVAILABLE','RESERVED','SOLD')),"
            + "  UNIQUE (brand, model)"
            + ");";

    public static final String CREATE_SALES_TABLE =
            "CREATE TABLE IF NOT EXISTS sales ("
            + "  sale_id      INTEGER PRIMARY KEY,"
            + "  car_id       INTEGER       NOT NULL,"
            + "  seller_name  VARCHAR(60)   NOT NULL,"
            + "  buyer_name   VARCHAR(60)   NOT NULL,"
            + "  sale_date    DATE          NOT NULL,"
            + "  price        DECIMAL(12,2) NOT NULL CHECK (price > 0),"
            + "  FOREIGN KEY (car_id) REFERENCES cars(car_id)"
            + ");";

    public static final String CREATE_AUDIT_TABLE =
            "CREATE TABLE IF NOT EXISTS audit_log ("
            + "  log_id      INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "  actor_name  VARCHAR(60)   NOT NULL,"
            + "  action      VARCHAR(100)  NOT NULL,"
            + "  created_at  TIMESTAMP     NOT NULL"
            + ");";

    public static final String CREATE_INDEXES =
            "CREATE INDEX IF NOT EXISTS idx_cars_brand  ON cars(brand);\n"
            + "CREATE INDEX IF NOT EXISTS idx_cars_status ON cars(status);\n"
            + "CREATE INDEX IF NOT EXISTS idx_sales_car   ON sales(car_id);";

    private SchemaInitializer() {}

    /**
     * Executes DDL to create required tables and seeds data if empty.
     *
     * @param connection active database connection
     * @throws RepositoryException if DDL execution or seeding fails
     */
    public static void init(Connection connection) throws RepositoryException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_CARS_TABLE);
            statement.execute(CREATE_SALES_TABLE);
            statement.execute(CREATE_AUDIT_TABLE);

            statement.execute("CREATE INDEX IF NOT EXISTS idx_cars_brand ON cars(brand);");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_cars_status ON cars(status);");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_sales_car ON sales(car_id);");
        } catch (SQLException e) {
            throw new RepositoryException("Failed to execute database DDL schema creation.", e);
        }

        seedIfEmpty(connection);
    }

    private static void seedIfEmpty(Connection connection) throws RepositoryException {
        // Seed cars if table is currently empty
        if (getRowCount(connection, "cars") == 0) {
            File seedCarsFile = new File("data/seed/cars.csv");
            if (!seedCarsFile.exists()) {
                seedCarsFile = new File("data/cars.csv");
            }
            if (seedCarsFile.exists()) {
                seedCarsFromCsv(connection, seedCarsFile);
            }
        }

        // Seed sales if table is currently empty
        if (getRowCount(connection, "sales") == 0) {
            File seedSalesFile = new File("data/seed/sales.csv");
            if (!seedSalesFile.exists()) {
                seedSalesFile = new File("data/sales.csv");
            }
            if (seedSalesFile.exists()) {
                seedSalesFromCsv(connection, seedSalesFile);
            }
        }
    }

    private static int getRowCount(Connection connection, String table) throws RepositoryException {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to count rows in table " + table, e);
        }
    }

    private static void seedCarsFromCsv(Connection connection, File file) throws RepositoryException {
        String insertSql = "INSERT INTO cars (car_id, brand, model, price, top_speed, fuel_type, horsepower, acceleration, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
             PreparedStatement ps = connection.prepareStatement(insertSql)) {

            String line;
            int lineNum = 0;
            boolean prevAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#") || (lineNum == 1 && (trimmed.toLowerCase().startsWith("id") || trimmed.toLowerCase().startsWith("carid")))) {
                        continue;
                    }
                    Car car = Car.fromCsv(trimmed, lineNum);
                    ps.setInt(1, car.getId());
                    ps.setString(2, car.getBrand());
                    ps.setString(3, car.getModel());
                    ps.setDouble(4, car.getPrice());
                    ps.setInt(5, car.getTopSpeed());
                    ps.setString(6, car.getFuelType().name());
                    ps.setInt(7, car.getHorsepower());
                    ps.setDouble(8, car.getAcceleration());
                    ps.setString(9, car.getStatus().name());
                    ps.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RepositoryException("Failed to seed cars into SQLite from " + file.getPath(), e);
            } finally {
                connection.setAutoCommit(prevAutoCommit);
            }
        } catch (Exception e) {
            if (e instanceof RepositoryException) throw (RepositoryException) e;
            throw new RepositoryException("I/O error during SQLite cars seeding: " + e.getMessage(), e);
        }
    }

    private static void seedSalesFromCsv(Connection connection, File file) throws RepositoryException {
        String insertSql = "INSERT INTO sales (sale_id, car_id, buyer_name, seller_name, sale_date, price) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
             PreparedStatement ps = connection.prepareStatement(insertSql)) {

            String line;
            int lineNum = 0;
            boolean prevAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#") || (lineNum == 1 && trimmed.toLowerCase().startsWith("saleid"))) {
                        continue;
                    }
                    Sale sale = Sale.fromCsv(trimmed, lineNum);
                    ps.setInt(1, sale.getSaleId());
                    ps.setInt(2, sale.getCarId());
                    ps.setString(3, sale.getBuyerName());
                    ps.setString(4, sale.getSellerName());
                    ps.setString(5, sale.getSaleDate().toString());
                    ps.setDouble(6, sale.getPrice());
                    ps.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RepositoryException("Failed to seed sales into SQLite from " + file.getPath(), e);
            } finally {
                connection.setAutoCommit(prevAutoCommit);
            }
        } catch (Exception e) {
            if (e instanceof RepositoryException) throw (RepositoryException) e;
            throw new RepositoryException("I/O error during SQLite sales seeding: " + e.getMessage(), e);
        }
    }
}
