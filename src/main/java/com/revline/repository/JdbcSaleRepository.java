package com.revline.repository;

import com.revline.model.Car;
import com.revline.model.Sale;
import com.revline.util.exception.RepositoryException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JDBC implementation of SaleRepository with relational transaction support (L17-L20).
 */
public class JdbcSaleRepository implements SaleRepository {

    private static final String SELECT_COLUMNS = "sale_id, car_id, buyer_name, seller_name, sale_date, price";

    private Connection connection;

    public JdbcSaleRepository(Connection connection) {
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
    public synchronized List<Sale> findAll() throws RepositoryException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sales ORDER BY sale_id";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return mapRows(rs);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load sales from JDBC storage.", e);
        }
    }

    @Override
    public synchronized Sale findById(Integer id) throws RepositoryException {
        if (id == null) return null;
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sales WHERE sale_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to query sale by ID.", e);
        }
    }

    @Override
    public synchronized List<Sale> findByCarId(int carId) throws RepositoryException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM sales WHERE car_id = ? ORDER BY sale_id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapRows(rs);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to query sales by car ID.", e);
        }
    }

    @Override
    public synchronized void save(Sale sale) throws RepositoryException {
        if (sale == null) return;
        String update = "UPDATE sales SET car_id=?, buyer_name=?, seller_name=?, sale_date=?, price=? WHERE sale_id=?";
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setInt(1, sale.getCarId());
            ps.setString(2, sale.getBuyerName());
            ps.setString(3, sale.getSellerName());
            ps.setString(4, sale.getSaleDate().toString());
            ps.setDouble(5, sale.getPrice());
            ps.setInt(6, sale.getSaleId());

            if (ps.executeUpdate() == 0) {
                insert(sale);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save sale to JDBC storage.", e);
        }
    }

    private void insert(Sale sale) throws SQLException {
        String sql = "INSERT INTO sales (sale_id, car_id, buyer_name, seller_name, sale_date, price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, sale.getSaleId());
            ps.setInt(2, sale.getCarId());
            ps.setString(3, sale.getBuyerName());
            ps.setString(4, sale.getSellerName());
            ps.setString(5, sale.getSaleDate().toString());
            ps.setDouble(6, sale.getPrice());
            ps.executeUpdate();
        }
    }

    @Override
    public synchronized boolean deleteById(Integer id) throws RepositoryException {
        if (id == null) return false;
        String sql = "DELETE FROM sales WHERE sale_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete sale from JDBC storage.", e);
        }
    }

    @Override
    public synchronized void saveAll(List<Sale> all) throws RepositoryException {
        if (all == null) return;
        boolean prev;
        try {
            prev = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement s = connection.createStatement()) {
                s.executeUpdate("DELETE FROM sales");
                for (Sale sale : all) {
                    insert(sale);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(prev);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save all sales atomically.", e);
        }
    }

    @Override
    public synchronized int count() throws RepositoryException {
        String sql = "SELECT COUNT(*) FROM sales";
        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to count sales.", e);
        }
    }

    @Override
    public synchronized int nextId() throws RepositoryException {
        String sql = "SELECT COALESCE(MAX(sale_id), 0) + 1 FROM sales";
        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 1;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to compute next sale ID.", e);
        }
    }

    @Override
    public synchronized void recordSale(Sale sale, Car car) throws RepositoryException {
        recordSaleAtomic(sale, car);
    }

    /**
     * Executes an ACID relational transaction combining car status update and sale insertion (L17-20).
     */
    public synchronized void recordSaleAtomic(Sale sale, Car car) throws RepositoryException {
        if (sale == null || car == null) {
            throw new IllegalArgumentException("Sale and Car cannot be null.");
        }

        boolean prevAutoCommit;
        try {
            prevAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                // Step 1: Update vehicle status to SOLD
                String updateCarSql = "UPDATE cars SET status = 'SOLD' WHERE car_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(updateCarSql)) {
                    ps.setInt(1, car.getId());
                    ps.executeUpdate();
                }

                // Step 2: Insert transaction record into sales ledger
                insert(sale);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RepositoryException("Relational transaction failed. Rollback executed.", e);
            } finally {
                connection.setAutoCommit(prevAutoCommit);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to execute atomic sale transaction.", e);
        }
    }

    private List<Sale> mapRows(ResultSet rs) throws SQLException {
        List<Sale> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
        return Collections.unmodifiableList(list);
    }

    private Sale mapRow(ResultSet rs) throws SQLException {
        int saleId = rs.getInt("sale_id");
        int carId = rs.getInt("car_id");
        String buyer = rs.getString("buyer_name");
        String seller = rs.getString("seller_name");
        String dateStr = rs.getString("sale_date");
        LocalDate date = (dateStr != null) ? LocalDate.parse(dateStr) : LocalDate.now();
        double price = rs.getDouble("price");

        return new Sale(saleId, carId, buyer, seller, date, price);
    }
}
