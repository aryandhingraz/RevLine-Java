package com.revline.repository;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.util.exception.RepositoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JDBC implementation of {@link CarRepository}, using prepared statements (L17-L20).
 * The supplied connection remains owned by the caller.
 */
public class JdbcCarRepository implements CarRepository {

    private static final String SELECT_COLUMNS =
            "car_id, brand, model, price, top_speed, fuel_type, horsepower, acceleration, status";

    private Connection connection;

    public JdbcCarRepository(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }
        this.connection = connection;
    }

    public synchronized void updateConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }
        this.connection = connection;
    }

    @Override
    public synchronized List<Car> findAll() throws RepositoryException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM cars ORDER BY car_id";
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            return mapRows(rows);
        } catch (SQLException e) {
            throw repositoryFailure("Failed to load cars from JDBC storage.", e);
        }
    }

    @Override
    public synchronized Car findById(Integer id) throws RepositoryException {
        if (id == null) {
            return null;
        }
        String sql = "SELECT " + SELECT_COLUMNS + " FROM cars WHERE car_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? mapRow(rows) : null;
            }
        } catch (SQLException e) {
            throw repositoryFailure("Failed to find car by ID.", e);
        }
    }

    @Override
    public synchronized void save(Car car) throws RepositoryException {
        if (car == null) {
            return;
        }
        String update = "UPDATE cars SET brand=?, model=?, price=?, top_speed=?, fuel_type=?, "
                + "horsepower=?, acceleration=?, status=? WHERE car_id=?";
        try (PreparedStatement statement = connection.prepareStatement(update)) {
            bindCar(statement, car, false);
            if (statement.executeUpdate() == 0) {
                insert(car);
            }
        } catch (SQLException e) {
            throw repositoryFailure("Failed to save car to JDBC storage.", e);
        }
    }

    private void insert(Car car) throws SQLException {
        String sql = "INSERT INTO cars (car_id, brand, model, price, top_speed, fuel_type, "
                + "horsepower, acceleration, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindCar(statement, car, true);
            statement.executeUpdate();
        }
    }

    private void bindCar(PreparedStatement statement, Car car, boolean includeId) throws SQLException {
        int first = includeId ? 1 : 0;
        if (includeId) {
            statement.setInt(1, car.getId());
        }
        statement.setString(first + 1, car.getBrand());
        statement.setString(first + 2, car.getModel());
        statement.setDouble(first + 3, car.getPrice());
        statement.setInt(first + 4, car.getTopSpeed());
        statement.setString(first + 5, car.getFuelType().name());
        statement.setInt(first + 6, car.getHorsepower());
        statement.setDouble(first + 7, car.getAcceleration());
        statement.setString(first + 8, car.getStatus().name());
        if (!includeId) {
            statement.setInt(9, car.getId());
        }
    }

    @Override
    public synchronized boolean deleteById(Integer id) throws RepositoryException {
        if (id == null) {
            return false;
        }
        String sql = "DELETE FROM cars WHERE car_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw repositoryFailure("Failed to delete car from JDBC storage.", e);
        }
    }

    @Override
    public synchronized void saveAll(List<Car> cars) throws RepositoryException {
        if (cars == null) {
            throw new IllegalArgumentException("Car list cannot be null.");
        }
        boolean previousAutoCommit;
        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM cars");
                for (Car car : cars) {
                    insert(car);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw repositoryFailure("Failed to replace JDBC car inventory.", e);
        }
    }

    @Override
    public synchronized int count() throws RepositoryException {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM cars")) {
            return rows.next() ? rows.getInt(1) : 0;
        } catch (SQLException e) {
            throw repositoryFailure("Failed to count JDBC inventory.", e);
        }
    }

    @Override
    public synchronized List<Car> findByStatus(Status status) throws RepositoryException {
        if (status == null) {
            return Collections.emptyList();
        }
        String sql = "SELECT " + SELECT_COLUMNS + " FROM cars WHERE status = ? ORDER BY car_id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            try (ResultSet rows = statement.executeQuery()) {
                return mapRows(rows);
            }
        } catch (SQLException e) {
            throw repositoryFailure("Failed to find cars by status.", e);
        }
    }

    /** Finds cars in an inclusive price range using bound parameters. */
    @Override
    public synchronized List<Car> findByPriceRange(double minPrice, double maxPrice) throws RepositoryException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM cars WHERE price BETWEEN ? AND ? ORDER BY car_id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, minPrice);
            statement.setDouble(2, maxPrice);
            try (ResultSet rows = statement.executeQuery()) {
                return mapRows(rows);
            }
        } catch (SQLException e) {
            throw repositoryFailure("Failed to find cars by price range.", e);
        }
    }

    @Override
    public synchronized int nextId() throws RepositoryException {
        String sql = "SELECT COALESCE(MAX(car_id), 0) + 1 FROM cars";
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            return rows.next() ? rows.getInt(1) : 1;
        } catch (SQLException e) {
            throw repositoryFailure("Failed to calculate the next car ID.", e);
        }
    }

    private List<Car> mapRows(ResultSet rows) throws SQLException {
        List<Car> cars = new ArrayList<>();
        while (rows.next()) {
            cars.add(mapRow(rows));
        }
        return Collections.unmodifiableList(cars);
    }

    private Car mapRow(ResultSet row) throws SQLException {
        return new Car(row.getInt("car_id"), row.getString("brand"), row.getString("model"),
                row.getDouble("price"), row.getInt("top_speed"),
                FuelType.valueOf(row.getString("fuel_type")), row.getInt("horsepower"),
                row.getDouble("acceleration"), Status.valueOf(row.getString("status")));
    }

    private RepositoryException repositoryFailure(String message, SQLException cause) {
        return new RepositoryException(message, cause);
    }
}
