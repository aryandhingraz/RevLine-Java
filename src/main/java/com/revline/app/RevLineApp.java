package com.revline.app;

import com.revline.repository.FileCarRepository;
import com.revline.repository.FileSaleRepository;
import com.revline.service.ComparisonService;
import com.revline.service.InventoryService;
import com.revline.service.RecentlyViewed;
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

/**
 * RevLine — Car Management System in Java.
 * "Drive Passion. Live RevLine."
 *
 * Main application entry point for the showroom management backend.
 */
public class RevLineApp {

    public static void main(String[] args) {
        // Enforce UTF-8 output on console for crisp box-drawing frames (L25, §3.4)
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        } catch (Exception ignored) {
            // Fallback to platform default if UTF-8 stream wrapping fails
        }

        try {
            // Step 1: Storage verification & default seed injection
            StorageBootstrap.BootInfo boot = StorageBootstrap.init();

            // Step 2: Initialize Core Logger & Repositories
            AppLogger logger = AppLogger.getInstance();
            FileCarRepository carRepo = new FileCarRepository(boot.carsFile);
            FileSaleRepository saleRepo = new FileSaleRepository(boot.salesFile);

            // Step 3: Initialize Domain Services & Utilities
            UndoManager undoManager = new UndoManager();
            InventoryService inventoryService = new InventoryService(carRepo, undoManager, logger);
            SearchService searchService = new SearchService(inventoryService);
            ReservationManager reservationManager = new ReservationManager();
            WaitlistService waitlistService = new WaitlistService();
            SalesService salesService = new SalesService(inventoryService, saleRepo, reservationManager, waitlistService, logger);
            ComparisonService comparisonService = new ComparisonService();
            RecentlyViewed recentlyViewed = new RecentlyViewed();

            // Step 4: Display C-style System Boot Panel & Brand Logo
            Banner.printBootPanel("CSV Flat-File (FileCarRepository & FileSaleRepository)",
                    boot.carsFile.getPath(), boot.wasCarsSeeded, inventoryService.count());
            Banner.printLogo();

            // Step 5: Start Interactive Controller
            InputReader reader = new InputReader();
            MenuController controller = new MenuController(
                    inventoryService,
                    searchService,
                    salesService,
                    comparisonService,
                    recentlyViewed,
                    logger,
                    reader
            );
            controller.run();

        } catch (RepositoryException e) {
            System.err.println("\n[CRITICAL ERROR] Failed to initialize storage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
