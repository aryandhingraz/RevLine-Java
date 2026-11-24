package com.revline.app;

import com.revline.concurrency.ConcurrencyLab;
import com.revline.service.ComparisonService;
import com.revline.service.DataToolsService;
import com.revline.service.InventoryService;
import com.revline.service.RecentlyViewed;
import com.revline.service.ReportService;
import com.revline.service.SalesService;
import com.revline.service.SearchService;
import com.revline.ui.InputReader;
import com.revline.ui.MenuPrinter;
import com.revline.util.AppLogger;
import com.revline.util.exception.InputClosedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller orchestrating console interactions for both Buyer and Seller views.
 * Coordinates modular handlers for buyer commands, seller actions, analytics, and data tools.
 */
public class MenuController {

    private final InventoryService inventoryService;
    private final SalesService salesService;
    private final AppLogger logger;
    private final InputReader reader;

    private final BuyerMenuHandler buyerHandler;
    private final SellerMenuHandler sellerHandler;
    private final ReportsMenuHandler reportsHandler;
    private final DataToolsMenuHandler dataToolsHandler;

    private boolean isSellerMode = false;

    public MenuController(InventoryService inventoryService,
                          SearchService searchService,
                          SalesService salesService,
                          ComparisonService comparisonService,
                          RecentlyViewed recentlyViewed,
                          ReportService reportService,
                          DataToolsService dataToolsService,
                          RevLineApp.StorageComponents storage,
                          AppLogger logger,
                          InputReader reader) {
        this.inventoryService = inventoryService;
        this.salesService = salesService;
        this.logger = logger;
        this.reader = reader;

        this.buyerHandler = new BuyerMenuHandler(inventoryService, searchService, salesService,
                comparisonService, recentlyViewed, reader);
        this.sellerHandler = new SellerMenuHandler(inventoryService, salesService, reader);
        this.reportsHandler = new ReportsMenuHandler(reportService, reader);
        this.dataToolsHandler = new DataToolsMenuHandler(dataToolsService, inventoryService, salesService, storage, reader);
    }

    public MenuController(InventoryService inventoryService,
                          SearchService searchService,
                          SalesService salesService,
                          ComparisonService comparisonService,
                          RecentlyViewed recentlyViewed,
                          ReportService reportService,
                          DataToolsService dataToolsService,
                          AppLogger logger,
                          InputReader reader) {
        this(inventoryService, searchService, salesService, comparisonService,
                recentlyViewed, reportService, dataToolsService, null, logger, reader);
    }

    /**
     * Main interactive event loop.
     */
    public void run() {
        try {
            boolean running = true;
            while (running) {
                running = isSellerMode ? processSellerTurn() : processBuyerTurn();
            }
            System.out.println("\n  Exiting RevLine System... Thank you for driving passion. Goodbye!\n");
        } catch (InputClosedException e) {
            System.out.println("\n  Input closed. Thank you for driving passion. Goodbye!\n");
        }
    }

    private boolean processSellerTurn() {
        MenuPrinter.printSellerView();
        String cmd = reader.readCommand("   Select Seller Option [1-9, A, S, 0]: ");
        if (cmd.equalsIgnoreCase("S")) {
            isSellerMode = false;
            System.out.println("\n  [✓] Switched to BUYER / CLIENT VIEW.");
            return true;
        }
        if (cmd.equals("0")) {
            return false;
        }
        handleSellerCommand(cmd);
        return true;
    }

    private boolean processBuyerTurn() {
        MenuPrinter.printBuyerView();
        String cmd = reader.readCommand("   Select Buyer Option [1-8, S, 0]: ");
        if (cmd.equalsIgnoreCase("S")) {
            isSellerMode = true;
            System.out.println("\n  [✓] Switched to SELLER / MANAGEMENT VIEW.");
            return true;
        }
        if (cmd.equals("0")) {
            return false;
        }
        buyerHandler.handleCommand(cmd);
        return true;
    }

    private void handleSellerCommand(String cmd) {
        switch (cmd) {
            case "1": sellerHandler.handleDisplayAllInventory(); break;
            case "2": sellerHandler.handleAddCar(); break;
            case "3": sellerHandler.handleUpdateCar(); break;
            case "4": sellerHandler.handleDeleteCar(); break;
            case "5": sellerHandler.handleUndo(); break;
            case "6": sellerHandler.handleSalesHistory(); break;
            case "7": reportsHandler.handleReportsMenu(); break;
            case "8": handleConcurrencyLabMenu(); break;
            case "9": dataToolsHandler.handleDataToolsMenu(); break;
            case "a":
            case "A": handleAuditLog(); break;
            default: System.out.println("  [!] Invalid choice. Please enter 1-9, A, S, or 0.");
        }
    }

    private void handleConcurrencyLabMenu() {
        boolean inLab = true;
        while (inLab) {
            MenuPrinter.printConcurrencyLabMenu();
            String choice = reader.readLine("  Select Option (0-5): ");
            switch (choice) {
                case "1": ConcurrencyLab.runDemo1RaceCondition(); break;
                case "2": ConcurrencyLab.runDemo2DoubleSellShowdown(salesService, inventoryService); break;
                case "3": ConcurrencyLab.runDemo3ThreadLifecycleTracer(); break;
                case "4": ConcurrencyLab.runDemo4ProducerConsumer(salesService); break;
                case "5": ConcurrencyLab.runDemo5InterruptionAndVisibility(inventoryService); break;
                case "0": inLab = false; break;
                default: System.out.println("  [!] Invalid choice. Please enter 0-5.");
            }
        }
    }

    private void handleAuditLog() {
        List<String> lines = (logger != null) ? logger.getRecentLines() : new ArrayList<>();
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║ SYSTEM AUDIT LOG (LAST %2d EVENTS) %62s ║\n", lines.size(), "");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════════════════════════╣");
        if (lines.isEmpty()) {
            System.out.println("║  No recent audit events logged.                                                                   ║");
        } else {
            for (String l : lines) {
                System.out.printf("║  %-96s ║\n", l);
            }
        }
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════════════════════════╝");
    }
}
