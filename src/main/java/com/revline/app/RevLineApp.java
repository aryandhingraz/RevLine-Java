package com.revline.app;

import com.revline.concurrency.AutoSaveThread;
import com.revline.concurrency.ReservationExpiryThread;
import com.revline.repository.CarRepository;
import com.revline.repository.DatabaseManager;
import com.revline.repository.FileCarRepository;
import com.revline.repository.FileSaleRepository;
import com.revline.repository.JdbcCarRepository;
import com.revline.repository.JdbcSaleRepository;
import com.revline.repository.SaleRepository;
import com.revline.service.ComparisonService;
import com.revline.service.DataToolsService;
import com.revline.service.InventoryService;
import com.revline.service.RecentlyViewed;
import com.revline.service.ReportService;
import com.revline.service.ReservationManager;
import com.revline.service.SalesService;
import com.revline.service.SearchService;
import com.revline.service.UndoManager;
import com.revline.service.WaitlistService;
import com.revline.ui.Banner;
import com.revline.ui.InputReader;
import com.revline.util.AppLogger;
import com.revline.util.AppPaths;
import com.revline.util.exception.RepositoryException;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

/**
 * RevLine — Car Management System in Java.
 * "Drive Passion. Live RevLine."
 *
 * Main application entry point for the showroom management backend.
 * Supports dual storage modes: CSV flat-file (default) or SQLite JDBC persistence (--storage=jdbc).
 */
public class RevLineApp {

    static class StorageComponents {
        final CarRepository carRepo;
        final SaleRepository saleRepo;
        Connection connection;
        final String engineName;
        final String storagePath;
        final boolean wasSeeded;

        StorageComponents(CarRepository carRepo, SaleRepository saleRepo, Connection connection,
                          String engineName, String storagePath, boolean wasSeeded) {
            this.carRepo = carRepo;
            this.saleRepo = saleRepo;
            this.connection = connection;
            this.engineName = engineName;
            this.storagePath = storagePath;
            this.wasSeeded = wasSeeded;
        }

        public synchronized boolean isJdbcActive() {
            return connection != null;
        }

        public synchronized Connection getConnection() {
            return connection;
        }

        public synchronized void closeDatabaseConnection() {
            if (connection != null) {
                DatabaseManager.closeQuietly(connection);
                connection = null;
            }
        }

        public synchronized void reconnectDatabase() throws RepositoryException {
            if (carRepo instanceof JdbcCarRepository && saleRepo instanceof JdbcSaleRepository) {
                closeDatabaseConnection();
                this.connection = StorageBootstrap.initDatabase();
                ((JdbcCarRepository) carRepo).updateConnection(this.connection);
                ((JdbcSaleRepository) saleRepo).updateConnection(this.connection);
            }
        }
    }

    private static class ServiceContainer {
        final InventoryService inventoryService;
        final SearchService searchService;
        final SalesService salesService;
        final ComparisonService comparisonService;
        final RecentlyViewed recentlyViewed;
        final ReportService reportService;
        final DataToolsService dataToolsService;

        ServiceContainer(InventoryService inventory, SearchService search, SalesService sales,
                         ComparisonService comparison, RecentlyViewed viewed, ReportService report,
                         DataToolsService dataTools) {
            this.inventoryService = inventory;
            this.searchService = search;
            this.salesService = sales;
            this.comparisonService = comparison;
            this.recentlyViewed = viewed;
            this.reportService = report;
            this.dataToolsService = dataTools;
        }
    }

    public static void main(String[] args) {
        initConsoleEncoding();
        boolean useJdbc = isJdbcRequested(args);
        StorageComponents storage = null;

        try {
            AppLogger logger = AppLogger.getInstance();
            storage = initStorage(useJdbc);

            ServiceContainer services = initServices(storage, logger);
            AutoSaveThread autoSave = startAutoSave(services.inventoryService, logger);
            Thread expiryThread = startReservationExpiry(services.salesService, logger);

            registerShutdownHook(autoSave, expiryThread, storage);
            displayWelcome(storage, services.inventoryService);
            runMenu(services, storage, logger);

        } catch (RepositoryException e) {
            handleFatalError(e);
        } finally {
            if (storage != null) {
                storage.closeDatabaseConnection();
            }
        }
    }

    private static void initConsoleEncoding() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        } catch (java.io.UnsupportedEncodingException e) {
            // Platform default encoding is retained if UTF-8 stream wrapping fails
        }
    }

    private static boolean isJdbcRequested(String[] args) {
        if (args == null) return false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--storage=jdbc") || arg.equalsIgnoreCase("--jdbc")) {
                return true;
            }
        }
        return false;
    }

    private static StorageComponents initStorage(boolean useJdbc) throws RepositoryException {
        if (useJdbc) {
            Connection conn = StorageBootstrap.initDatabase();
            CarRepository carRepo = new JdbcCarRepository(conn);
            SaleRepository saleRepo = new JdbcSaleRepository(conn);
            String engine = "SQLite Relational Database (JdbcCarRepository & JdbcSaleRepository)";
            return new StorageComponents(carRepo, saleRepo, conn, engine, DatabaseManager.DEFAULT_DB_PATH, false);
        } else {
            StorageBootstrap.BootInfo boot = StorageBootstrap.init();
            CarRepository carRepo = new FileCarRepository(boot.carsFile);
            SaleRepository saleRepo = new FileSaleRepository(boot.salesFile);
            String engine = "CSV Flat-File (FileCarRepository & FileSaleRepository)";
            return new StorageComponents(carRepo, saleRepo, null, engine, boot.carsFile.getPath(), boot.wasCarsSeeded);
        }
    }

    private static ServiceContainer initServices(StorageComponents storage, AppLogger logger) throws RepositoryException {
        UndoManager undo = new UndoManager();
        InventoryService inventory = new InventoryService(storage.carRepo, undo, logger);
        int resetCount = inventory.resetStaleReservations();
        if (resetCount > 0 && logger != null) {
            logger.log("APP", "STARTUP", "Auto-reset " + resetCount + " stale RESERVED car(s) to AVAILABLE.");
        }
        SearchService search = new SearchService(inventory);
        ReservationManager reservations = new ReservationManager();
        WaitlistService waitlist = new WaitlistService();
        SalesService sales = new SalesService(inventory, storage.saleRepo, reservations, waitlist, logger);
        ComparisonService comparison = new ComparisonService();
        RecentlyViewed viewed = new RecentlyViewed();
        ReportService reports = new ReportService(inventory, storage.saleRepo);
        DataToolsService dataTools = new DataToolsService(logger);

        return new ServiceContainer(inventory, search, sales, comparison, viewed, reports, dataTools);
    }

    private static AutoSaveThread startAutoSave(InventoryService inventory, AppLogger logger) {
        AutoSaveThread thread = new AutoSaveThread(inventory, logger);
        thread.start();
        return thread;
    }

    private static Thread startReservationExpiry(SalesService sales, AppLogger logger) {
        ReservationExpiryThread task = new ReservationExpiryThread(sales, logger);
        Thread thread = new Thread(task, "ReservationExpiryThread");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static void registerShutdownHook(AutoSaveThread autoSave, Thread expiryThread, StorageComponents storage) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            autoSave.shutdown();
            expiryThread.interrupt();
            try {
                autoSave.join(1000);
                expiryThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (storage != null) {
                storage.closeDatabaseConnection();
            }
        }, "RevLine-ShutdownHook"));
    }

    private static void displayWelcome(StorageComponents storage, InventoryService inventory) {
        Banner.printBootPanel(storage.engineName, storage.storagePath, storage.wasSeeded, inventory.count());
        Banner.printLogo();
    }

    private static void runMenu(ServiceContainer s, StorageComponents storage, AppLogger logger) {
        InputReader reader = new InputReader();
        MenuController controller = new MenuController(
                s.inventoryService, s.searchService, s.salesService, s.comparisonService,
                s.recentlyViewed, s.reportService, s.dataToolsService, storage, logger, reader
        );
        controller.run();
    }

    private static void handleFatalError(RepositoryException e) {
        System.err.println("\n[CRITICAL ERROR] Failed to initialize storage: " + e.getMessage());
        AppLogger errorLogger = new AppLogger(new File(AppPaths.ERROR_LOG));
        errorLogger.logError("SYSTEM", "STORAGE_INIT_FAIL", e);
    }
}
