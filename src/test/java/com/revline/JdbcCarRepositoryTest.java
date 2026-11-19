package com.revline;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.repository.JdbcCarRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/** Integration checks for JDBC car queries and exercise EX-6. */
public final class JdbcCarRepositoryTest {

    private JdbcCarRepositoryTest() {}

    public static void testExercise6FindByPriceRange() throws Exception {
        Class.forName("org.sqlite.JDBC");
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            createCarsTable(connection);
            insertCar(connection, 1, "Apex", "One", 100000.0);
            insertCar(connection, 2, "Apex", "Two", 200000.0);
            insertCar(connection, 3, "Boreal", "Three", 300000.0);

            JdbcCarRepository repository = new JdbcCarRepository(connection);
            List<Car> matches = repository.findByPriceRange(100000.0, 200000.0);

            Assert.assertEquals(2, matches.size(), "Price range includes both boundary values");
            Assert.assertEquals(1, matches.get(0).getId(), "Results are ordered by car ID");
            Assert.assertEquals(2, matches.get(1).getId(), "Second inclusive range result has ID 2");
            Assert.assertTrue(repository.findByPriceRange(900000.0, 1000000.0).isEmpty(),
                    "A range with no matching cars returns an empty list");

            Car saved = new Car(4, "Crest", "Four", 400000.0, 350,
                    FuelType.ELECTRIC, 900, 2.5, Status.AVAILABLE);
            repository.save(saved);
            Assert.assertEquals(4, repository.count(), "Save inserts a new JDBC car");
            Assert.assertEquals(5, repository.nextId(), "Next ID is max existing ID plus one");
            Assert.assertEquals("Crest", repository.findById(4).getBrand(), "Find by ID maps brand correctly");
            Assert.assertTrue(repository.deleteById(4), "Delete removes an existing JDBC car");
        }
    }

    private static void createCarsTable(Connection connection) throws Exception {
        String sql = "CREATE TABLE cars (car_id INTEGER PRIMARY KEY, brand TEXT NOT NULL, model TEXT NOT NULL, "
                + "price REAL NOT NULL, top_speed INTEGER NOT NULL, fuel_type TEXT NOT NULL, "
                + "horsepower INTEGER NOT NULL, acceleration REAL NOT NULL, status TEXT NOT NULL)";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static void insertCar(Connection connection, int id, String brand, String model, double price)
            throws Exception {
        String sql = "INSERT INTO cars VALUES (?, ?, ?, ?, 320, 'HYBRID', 800, 2.8, 'AVAILABLE')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setString(2, brand);
            statement.setString(3, model);
            statement.setDouble(4, price);
            statement.executeUpdate();
        }
    }
}
