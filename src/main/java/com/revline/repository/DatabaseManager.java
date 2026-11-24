package com.revline.repository;

import com.revline.util.AppPaths;
import com.revline.util.FileUtil;
import com.revline.util.exception.RepositoryException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite JDBC database connections and configuration (L17-L18).
 * Ensures the SQLite JDBC driver is loaded and foreign key constraints are enabled.
 */
public final class DatabaseManager {

    public static final String DEFAULT_DB_PATH = AppPaths.RUNTIME_DB;
    public static final String DEFAULT_JDBC_URL = "jdbc:sqlite:" + DEFAULT_DB_PATH;
    public static final String IN_MEMORY_URL = "jdbc:sqlite::memory:";
    public static final String DRIVER_CLASS_NAME = "org.sqlite.JDBC";

    private static boolean driverLoaded = false;

    private DatabaseManager() {}

    /**
     * Explicitly loads and registers the SQLite JDBC driver with DriverManager (L17).
     *
     * @throws RepositoryException if driver class cannot be found or loaded
     */
    public static synchronized void loadDriver() throws RepositoryException {
        if (!driverLoaded) {
            try {
                Class.forName(DRIVER_CLASS_NAME);
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new RepositoryException("SQLite JDBC driver not found on classpath: " + DRIVER_CLASS_NAME, e);
            }
        }
    }

    /**
     * Obtains a new database connection using the default database file (data/revline.db).
     * Automatically enables SQLite foreign key enforcement (PRAGMA foreign_keys = ON).
     *
     * @return active database Connection
     * @throws RepositoryException on connection failure
     */
    public static Connection getConnection() throws RepositoryException {
        return getConnection(DEFAULT_JDBC_URL);
    }

    /**
     * Obtains a database connection to a specified JDBC URL.
     *
     * @param jdbcUrl target JDBC connection string (e.g. jdbc:sqlite:data/revline.db or jdbc:sqlite::memory:)
     * @return active database Connection
     * @throws RepositoryException on connection failure
     */
    public static Connection getConnection(String jdbcUrl) throws RepositoryException {
        loadDriver();
        ensureDirectoryForUrl(jdbcUrl);

        try {
            Connection connection = DriverManager.getConnection(jdbcUrl);
            // Syllabus requirement (L17-18): enforce foreign key relational integrity
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON;");
            }
            return connection;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to establish SQLite JDBC connection to: " + jdbcUrl, e);
        }
    }

    /**
     * Closes any database resource without throwing checked exceptions (delegates to FileUtil.closeQuietly).
     */
    public static void closeQuietly(AutoCloseable resource) {
        FileUtil.closeQuietly(resource);
    }

    private static void ensureDirectoryForUrl(String jdbcUrl) {
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:sqlite:") && !jdbcUrl.contains(":memory:")) {
            String path = jdbcUrl.substring("jdbc:sqlite:".length());
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
        }
    }
}
