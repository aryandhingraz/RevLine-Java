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
import com.revline.util.exception.RepositoryException;

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

    public static void main(String[] args) {
        // Enforce UTF-8 output on console for crisp box-drawing frames (L25, §3.4)
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        } catch (Exception ignored) {
            // Fallback to platform default if UTF-8 stream wrapping fails
        }

        boolean useJdbc = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--storage=jdbc") || arg.equalsIgnoreCase("--jdbc")) {
                useJdbc = true;
                break;
            }
        }

        Connection jdbcConnection = null;
        try {
            AppLogger logger = AppLogger.getInstance();
            CarRepository carRepo;
            SaleRepository saleRepo;
            String engineName;
            String storagePath;
            boolean wasSeeded = false;

            if (useJdbc) {
                // Initialize SQLite JDBC Persistence (Phase 4, L17-L20)
                jdbcConnection = StorageBootstrap.initDatabase();
                carRepo = new JdbcCarRepository(jdbcConnection);
                saleRepo = new JdbcSaleRepository(jdbcConnection);
                engineName = "SQLite Relational Database (JdbcCarRepository & JdbcSaleRepository)";
                storagePath = DatabaseManager.DEFAULT_DB_PATH;
            } else {
                // Initialize CSV Flat-File Persistence (Phase 2 & 3, L26-L27)
                StorageBootstrap.BootInfo boot = StorageBootstrap.init();
                carRepo = new FileCarRepository(boot.carsFile);
                saleRepo = new FileSaleRepository(boot.salesFile);
                engineName = "CSV Flat-File (FileCarRepository & FileSaleRepository)";
                storagePath = boot.carsFile.getPath();
                wasSeeded = boot.wasCarsSeeded;
            }

            // Initialize Domain Services & Utilities
            UndoManager undoManager = new UndoManager();
            InventoryService inventoryService = new InventoryService(carRepo, undoManager, logger);
            SearchService searchService = new SearchService(inventoryService);
            ReservationManager reservationManager = new ReservationManager();
            WaitlistService waitlistService = new WaitlistService();
            SalesService salesService = new SalesService(inventoryService, saleRepo, reservationManager, waitlistService, logger);
            ComparisonService comparisonService = new ComparisonService();
            RecentlyViewed recentlyViewed = new RecentlyViewed();
            ReportService reportService = new ReportService(inventoryService, saleRepo);
            DataToolsService dataToolsService = new DataToolsService(logger);

            // Initialize & Launch Background Daemon Threads (Phase 6, L9-L12)
            AutoSaveThread autoSaveThread = new AutoSaveThread(inventoryService, logger);
            autoSaveThread.start();

            ReservationExpiryThread expiryRunnable = new ReservationExpiryThread(salesService, logger);
            Thread expiryThread = new Thread(expiryRunnable, "ReservationExpiryThread");
            expiryThread.setDaemon(true);
            expiryThread.start();

            // JVM Graceful Shutdown Hook (L10)
            final Connection connForHook = jdbcConnection;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                autoSaveThread.shutdown();
                expiryRunnable.shutdown();
                expiryThread.interrupt();
                try {
                    autoSaveThread.join(1000);
                    expiryThread.join(1000);
                } catch (InterruptedException ignored) {}
                DatabaseManager.closeQuietly(connForHook);
            }, "RevLine-ShutdownHook"));

            // Display C-style System Boot Panel & Brand Logo
            Banner.printBootPanel(engineName, storagePath, wasSeeded, inventoryService.count());
            Banner.printLogo();

            // Start Interactive Controller
            InputReader reader = new InputReader();
            MenuController controller = new MenuController(
                    inventoryService,
                    searchService,
                    salesService,
                    comparisonService,
                    recentlyViewed,
                    reportService,
                    dataToolsService,
                    logger,
                    reader
            );
            controller.run();

        } catch (RepositoryException e) {
            System.err.println("\n[CRITICAL ERROR] Failed to initialize storage: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeQuietly(jdbcConnection);
        }
    }
}
