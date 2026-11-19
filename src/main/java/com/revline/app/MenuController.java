package com.revline.app;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Reservation;
import com.revline.model.Sale;
import com.revline.model.Status;
import com.revline.service.CarComparators;
import com.revline.service.ComparisonService;
import com.revline.service.FilterBuilder;
import com.revline.service.InventoryService;
import com.revline.service.RecentlyViewed;
import com.revline.service.SalesService;
import com.revline.service.SearchService;
import com.revline.ui.ConsoleTable;
import com.revline.ui.InputReader;
import com.revline.ui.MenuPrinter;
import com.revline.util.AppLogger;
import com.revline.util.exception.CarNotAvailableException;
import com.revline.util.exception.CarNotFoundException;
import com.revline.util.exception.InvalidCarException;
import com.revline.util.exception.RepositoryException;
import com.revline.util.exception.RevLineException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Controller orchestrating console interactions for both Buyer and Seller views.
 * Supports view toggling [S], spec comparison, reservations, sales, and undo operations.
 */
public class MenuController {

    private final InventoryService inventoryService;
    private final SearchService searchService;
    private final SalesService salesService;
    private final ComparisonService comparisonService;
    private final RecentlyViewed recentlyViewed;
    private final AppLogger logger;
    private final InputReader reader;

    private boolean isSellerMode = false;

    public MenuController(InventoryService inventoryService,
                          SearchService searchService,
                          SalesService salesService,
                          ComparisonService comparisonService,
                          RecentlyViewed recentlyViewed,
                          AppLogger logger,
                          InputReader reader) {
        this.inventoryService = inventoryService;
        this.searchService = searchService;
        this.salesService = salesService;
        this.comparisonService = comparisonService;
        this.recentlyViewed = recentlyViewed;
        this.logger = logger;
        this.reader = reader;
    }

    /**
     * Main interactive event loop.
     */
    public void run() {
        boolean running = true;
        while (running) {
            if (isSellerMode) {
                MenuPrinter.printSellerView();
                String cmd = reader.readCommand("   Select Seller Option [1-8, S, 0]: ");
                if (cmd.equalsIgnoreCase("S")) {
                    isSellerMode = false;
                    System.out.println("\n  [✓] Switched to BUYER / CLIENT VIEW.");
                    continue;
                }
                if (cmd.equals("0")) {
                    running = false;
                    break;
                }
                handleSellerCommand(cmd);
            } else {
                MenuPrinter.printBuyerView();
                String cmd = reader.readCommand("   Select Buyer Option [1-8, S, 0]: ");
                if (cmd.equalsIgnoreCase("S")) {
                    isSellerMode = true;
                    System.out.println("\n  [✓] Switched to SELLER / MANAGEMENT VIEW.");
                    continue;
                }
                if (cmd.equals("0")) {
                    running = false;
                    break;
                }
                handleBuyerCommand(cmd);
            }
        }
        System.out.println("\n  Exiting RevLine System... Thank you for driving passion. Goodbye!\n");
    }

    // =========================================================================
    // BUYER ACTIONS
    // =========================================================================

    private void handleBuyerCommand(String cmd) {
        switch (cmd) {
            case "1":
                handleBrowseShowroom();
                break;
            case "2":
                handleSearchAndFilter();
                break;
            case "3":
                handleSortShowroom();
                break;
            case "4":
                handleCompareCars();
                break;
            case "5":
                handleReserveCar();
                break;
            case "6":
                handlePurchaseCar();
                break;
            case "7":
                handleJoinWaitlist();
                break;
            case "8":
                handleRecentlyViewed();
                break;
            default:
                System.out.println("  [!] Invalid choice. Please enter 1-8, S, or 0.");
        }
    }

    private void handleBrowseShowroom() {
        List<Car> cars = inventoryService.findAll();
        if (cars.isEmpty()) {
            System.out.println("\n  [i] Showroom is currently empty.");
            return;
        }

        System.out.println("\n--- INTERACTIVE SHOWROOM BROWSER (L6-7 ListIterator) ---");
        System.out.println("Commands: [N] Next, [P] Previous, [Q] Quit browsing");

        ListIterator<Car> iterator = cars.listIterator();
        while (iterator.hasNext()) {
            Car current = iterator.next();
            recentlyViewed.recordView(current);
            displayCarDetailCard(current, iterator.nextIndex(), cars.size());

            boolean browsing = true;
            while (browsing) {
                String input = reader.readCommand("  [N]ext / [P]revious / [Q]uit: ");
                if (input.equalsIgnoreCase("N")) {
                    if (!iterator.hasNext()) {
                        System.out.println("  [i] You are at the end of the showroom.");
                    } else {
                        browsing = false;
                    }
                } else if (input.equalsIgnoreCase("P")) {
                    // Step back two steps to show the previous car
                    if (iterator.hasPrevious()) {
                        iterator.previous(); // step back over current
                        if (iterator.hasPrevious()) {
                            current = iterator.previous();
                            recentlyViewed.recordView(current);
                            iterator.next(); // align forward iterator position
                            displayCarDetailCard(current, iterator.nextIndex(), cars.size());
                        } else {
                            iterator.next(); // restore position
                            System.out.println("  [i] You are at the first vehicle.");
                        }
                    }
                } else if (input.equalsIgnoreCase("Q")) {
                    return;
                } else {
                    System.out.println("  [!] Please enter N, P, or Q.");
                }
            }
        }
    }

    private void displayCarDetailCard(Car c, int currentIdx, int total) {
        System.out.println();
        System.out.println("  +-------------------------------------------------------------+");
        System.out.printf("  | VEHICLE %2d of %-2d : %-42s |\n", currentIdx, total, c.getBrand() + " " + c.getModel());
        System.out.println("  +-------------------------------------------------------------+");
        System.out.printf("  |  Car ID       : #%-43d |\n", c.getId());
        System.out.printf("  |  Price        : $%,-42.2f |\n", c.getPrice());
        System.out.printf("  |  Top Speed    : %d km/h %-35s |\n", c.getTopSpeed(), "");
        System.out.printf("  |  Horsepower   : %d HP %-37s |\n", c.getHorsepower(), "");
        System.out.printf("  |  Acceleration : %.2f s (0-100 km/h) %-28s |\n", c.getAcceleration(), "");
        System.out.printf("  |  Fuel Type    : %-43s |\n", c.getFuelType());
        System.out.printf("  |  Status       : %-43s |\n", c.getStatus());
        System.out.println("  +-------------------------------------------------------------+");
    }

    private void handleSearchAndFilter() {
        MenuPrinter.printSearchFilterMenu();
        int choice = reader.readInt("   Select search/filter mode (0-5): ", 0, 5);
        if (choice == 0) return;

        List<Car> all = inventoryService.findAll();
        List<Car> results = new ArrayList<>();

        switch (choice) {
            case 1: {
                int id = reader.readInt("  Enter exact Car ID to find: ", 1, 999999);
                try {
                    Car found = searchService.findById(id);
                    results.add(found);
                    recentlyViewed.recordView(found);
                } catch (CarNotFoundException e) {
                    System.out.println("\n  [!] " + e.getMessage());
                }
                break;
            }
            case 2: {
                String query = reader.readLine("  Enter search text (e.g. 'Ferrari', 'Hybrid', '911'): ");
                try {
                    results = searchService.search(query);
                    for (Car c : results) {
                        recentlyViewed.recordView(c);
                    }
                } catch (UnsupportedOperationException e) {
                    System.out.println("\n  [!] EX-3 is pending student implementation: " + e.getMessage());
                    return;
                }
                break;
            }
            case 3: {
                double min = reader.readDouble("  Enter Minimum Budget ($): ", 0.0, 50000000.0);
                double max = reader.readDouble("  Enter Maximum Budget ($): ", min, 50000000.0);
                FilterBuilder fb = new FilterBuilder().minPrice(min).maxPrice(max);
                results = fb.filter(all);
                break;
            }
            case 4: {
                int minSpeed = reader.readInt("  Enter Minimum Top Speed (km/h): ", 0, 600);
                int minHp = reader.readInt("  Enter Minimum Horsepower (HP): ", 0, 3000);
                FilterBuilder fb = new FilterBuilder().minSpeed(minSpeed).minHorsepower(minHp);
                results = fb.filter(all);
                break;
            }
            case 5: {
                FilterBuilder fb = new FilterBuilder().availableOnly();
                results = fb.filter(all);
                break;
            }
        }

        ConsoleTable.render("SEARCH RESULTS (" + results.size() + " MATCHES)", results);
    }

    private void handleSortShowroom() {
        MenuPrinter.printSortMenu();
        int sortChoice = reader.readInt("   Select sort order (0-7): ", 0, 7);
        if (sortChoice == 0) return;

        Comparator<Car> comp;
        String title;

        switch (sortChoice) {
            case 1:
                comp = CarComparators.BY_PRICE_ASC;
                title = "SHOWROOM SORTED: PRICE (LOW TO HIGH)";
                break;
            case 2:
                comp = CarComparators.BY_PRICE_DESC;
                title = "SHOWROOM SORTED: PRICE (HIGH TO LOW)";
                break;
            case 3:
                comp = CarComparators.BY_SPEED_DESC;
                title = "SHOWROOM SORTED: TOP SPEED (FASTEST FIRST)";
                break;
            case 4:
                comp = CarComparators.BY_HP_DESC;
                title = "SHOWROOM SORTED: HORSEPOWER (MOST POWERFUL FIRST)";
                break;
            case 5:
                comp = CarComparators.BY_ACCEL_ASC;
                title = "SHOWROOM SORTED: ACCELERATION (0-100 QUICKEST FIRST)";
                break;
            case 6:
                comp = CarComparators.BY_BRAND_MODEL;
                title = "SHOWROOM SORTED: BRAND & MODEL (ALPHABETICAL)";
                break;
            case 7:
                comp = CarComparators.byValueForMoney();
                title = "SHOWROOM SORTED: VALUE FOR MONEY [EX-2]";
                break;
            default:
                return;
        }

        List<Car> sorted = new ArrayList<>(inventoryService.findAll());
        sorted.sort(comp);
        ConsoleTable.render(title, sorted);
    }

    private void handleCompareCars() {
        System.out.println("\n--- SIDE-BY-SIDE VEHICLE COMPARISON (L2 Matrix) ---");
        int count = reader.readInt("  How many cars to compare (2, 3, or 4)? ", 2, 4);

        List<Car> selected = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            while (true) {
                int id = reader.readInt("  Enter ID for Car #" + i + ": ", 1, 999999);
                try {
                    Car c = inventoryService.findById(id);
                    selected.add(c);
                    recentlyViewed.recordView(c);
                    break;
                } catch (CarNotFoundException e) {
                    System.out.println("  [!] " + e.getMessage() + " Please enter a valid ID.");
                }
            }
        }

        ComparisonService.ComparisonResult result = comparisonService.compare(selected);
        System.out.println();
        System.out.print(result.formatTable());
    }

    private void handleReserveCar() {
        System.out.println("\n--- PLACE VEHICLE RESERVATION ---");
        int carId = reader.readInt("  Enter Car ID to reserve: ", 1, 999999);
        String buyer = reader.readLine("  Enter Reserving Buyer Name: ");
        int minutes = reader.readInt("  Enter Hold Duration in minutes (1-1440): ", 1, 1440);

        try {
            Reservation res = salesService.reserveCar(carId, buyer, minutes);
            System.out.println();
            System.out.println("╔════════════════════════════════════════════════════════════════════╗");
            System.out.println("║ RESERVATION CONFIRMED                                              ║");
            System.out.println("╠════════════════════════════════════════════════════════════════════╣");
            System.out.printf("║  Car ID      : #%-51d ║\n", res.getCarId());
            System.out.printf("║  Reserved By : %-52s ║\n", res.getBuyerName());
            System.out.printf("║  Expires At  : %-52s ║\n", res.getExpiresAt());
            System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        } catch (RevLineException e) {
            System.out.println("\n  [!] Reservation failed: " + e.getMessage());
        }
    }

    private void handlePurchaseCar() {
        System.out.println("\n--- EXECUTE VEHICLE SALE ---");
        int carId = reader.readInt("  Enter Car ID to purchase: ", 1, 999999);

        try {
            Car car = inventoryService.findById(carId);
            if (car.getStatus() == Status.SOLD) {
                System.out.println("\n  [!] Vehicle #" + carId + " is already SOLD.");
                return;
            }

            System.out.printf("  Selected Vehicle: %s %s | Price: $%,.2f\n", car.getBrand(), car.getModel(), car.getPrice());
            String buyer = reader.readLine("  Enter Buyer Full Name: ");
            String seller = reader.readLine("  Enter Sales Representative Name (or RevLine): ");
            if (seller.trim().isEmpty()) {
                seller = "RevLine Showroom";
            }

            boolean confirm = reader.confirm("  Confirm purchase for $" + String.format("%,.2f", car.getPrice()) + "?");
            if (!confirm) {
                System.out.println("  Purchase cancelled by user.");
                return;
            }

            Sale sale = salesService.sellCar(carId, buyer, seller);
            System.out.println();
            System.out.print(salesService.generateReceipt(sale, car));

        } catch (RevLineException e) {
            System.out.println("\n  [!] Purchase failed: " + e.getMessage());
        }
    }

    private void handleJoinWaitlist() {
        System.out.println("\n--- JOIN CUSTOMER WAITLIST (FIFO Priority) ---");
        int carId = reader.readInt("  Enter Car ID: ", 1, 999999);
        String buyer = reader.readLine("  Enter Your Full Name: ");

        int pos = salesService.getWaitlistService().join(carId, buyer);
        System.out.printf("\n  [✓] %s added to waitlist for Car #%d! Current position in queue: #%d\n", buyer, carId, pos);
    }

    private void handleRecentlyViewed() {
        List<Car> viewed = recentlyViewed.getRecentlyViewedList();
        ConsoleTable.render("RECENTLY VIEWED SUPERCARS (LRU 5)", viewed);
    }

    // =========================================================================
    // SELLER ACTIONS
    // =========================================================================

    private void handleSellerCommand(String cmd) {
        switch (cmd) {
            case "1":
                handleDisplayAllInventory();
                break;
            case "2":
                handleAddCar();
                break;
            case "3":
                handleUpdateCar();
                break;
            case "4":
                handleDeleteCar();
                break;
            case "5":
                handleUndo();
                break;
            case "6":
                handleSalesHistory();
                break;
            case "7":
                handleShowroomSummary();
                break;
            case "8":
                handleAuditLog();
                break;
            default:
                System.out.println("  [!] Invalid choice. Please enter 1-8, S, or 0.");
        }
    }

    private void handleDisplayAllInventory() {
        List<Car> all = inventoryService.findAll();
        ConsoleTable.render("DATA: COMPLETE SHOWROOM INVENTORY (" + all.size() + " VEHICLES)", all);
    }

    private void handleAddCar() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ ADD A NEW SUPERCAR                                                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");

        try {
            String brand = reader.readLine("  Enter Brand (e.g. Bugatti): ");
            String model = reader.readLine("  Enter Model (e.g. Tourbillon): ");
            double price = reader.readDouble("  Enter Price in USD ($): ", 1000.0, 50000000.0);
            int speed = reader.readInt("  Enter Top Speed in km/h (50-600): ", 50, 600);

            System.out.println("  Fuel Types: [1] PETROL  [2] HYBRID  [3] ELECTRIC");
            int fuelChoice = reader.readInt("  Select Fuel Type (1-3): ", 1, 3);
            FuelType fuelType = (fuelChoice == 1) ? FuelType.PETROL :
                    (fuelChoice == 2) ? FuelType.HYBRID : FuelType.ELECTRIC;

            int hp = reader.readInt("  Enter Horsepower in HP (50-3000): ", 50, 3000);
            double accel = reader.readDouble("  Enter 0-100 Acceleration in seconds (1.0-20.0): ", 1.0, 20.0);

            Car newCar = new Car(0, brand, model, price, speed, fuelType, hp, accel, Status.AVAILABLE);
            Car saved = inventoryService.addCar(newCar);

            System.out.println();
            System.out.println("╔════════════════════════════════════════════════════════════════════╗");
            System.out.printf("║ SUCCESS: '%s %s' added to showroom! %-24s ║\n", saved.getBrand(), saved.getModel(), "");
            System.out.printf("║ Assigned Car ID: %-50d ║\n", saved.getId());
            System.out.println("╚════════════════════════════════════════════════════════════════════╝");
        } catch (InvalidCarException | RepositoryException e) {
            System.out.println("\n  [!] Failed to add vehicle: " + e.getMessage());
        }
    }

    private void handleUpdateCar() {
        System.out.println("\n--- UPDATE SUPERCAR SPECIFICATIONS ---");
        int id = reader.readInt("  Enter Car ID to update: ", 1, 999999);

        try {
            Car existing = inventoryService.findById(id);
            if (existing.getStatus() != Status.AVAILABLE) {
                System.out.println("  [!] Cannot update vehicle with status " + existing.getStatus() + "; must be AVAILABLE.");
                return;
            }

            System.out.printf("  Current: %s %s | $%,.2f | %d km/h | %d HP | %.2f s | %s\n",
                    existing.getBrand(), existing.getModel(), existing.getPrice(),
                    existing.getTopSpeed(), existing.getHorsepower(), existing.getAcceleration(),
                    existing.getFuelType());

            String brand = reader.readLine("  Enter New Brand [" + existing.getBrand() + "]: ");
            String model = reader.readLine("  Enter New Model [" + existing.getModel() + "]: ");
            double price = reader.readDouble("  Enter New Price ($) [" + String.format("%.2f", existing.getPrice()) + "]: ", 1000.0, 50000000.0);
            int speed = reader.readInt("  Enter New Top Speed (km/h) [" + existing.getTopSpeed() + "]: ", 50, 600);

            System.out.println("  Fuel Types: [1] PETROL  [2] HYBRID  [3] ELECTRIC");
            int fuelChoice = reader.readInt("  Select Fuel Type (1-3): ", 1, 3);
            FuelType fuelType = (fuelChoice == 1) ? FuelType.PETROL :
                    (fuelChoice == 2) ? FuelType.HYBRID : FuelType.ELECTRIC;

            int hp = reader.readInt("  Enter New Horsepower [" + existing.getHorsepower() + "]: ", 50, 3000);
            double accel = reader.readDouble("  Enter New 0-100 Acceleration [" + existing.getAcceleration() + "]: ", 1.0, 20.0);

            Car updated = new Car(id, brand, model, price, speed, fuelType, hp, accel, existing.getStatus());
            inventoryService.updateCar(id, updated);

            System.out.println("\n  [✓] Vehicle #" + id + " specifications updated successfully.");
        } catch (RevLineException e) {
            System.out.println("\n  [!] Update failed: " + e.getMessage());
        }
    }

    private void handleDeleteCar() {
        System.out.println("\n--- DELETE SUPERCAR FROM INVENTORY ---");
        int id = reader.readInt("  Enter Car ID to delete: ", 1, 999999);

        try {
            Car existing = inventoryService.findById(id);
            if (existing.getStatus() != Status.AVAILABLE) {
                System.out.println("  [!] Cannot delete vehicle with status " + existing.getStatus() + "; must be AVAILABLE.");
                return;
            }

            boolean confirm = reader.confirm("  Are you sure you want to delete '" + existing.getBrand() + " " + existing.getModel() + "'?");
            if (!confirm) {
                System.out.println("  Deletion cancelled.");
                return;
            }

            inventoryService.deleteCar(id);
            System.out.println("\n  [✓] Vehicle #" + id + " deleted from showroom. (Can be restored via Undo)");
        } catch (RevLineException e) {
            System.out.println("\n  [!] Deletion failed: " + e.getMessage());
        }
    }

    private void handleUndo() {
        System.out.println("\n--- UNDO LAST INVENTORY OPERATION [EX-7] ---");
        try {
            String message = inventoryService.getUndoManager().undo(inventoryService);
            System.out.println("\n  [✓] " + message);
        } catch (RevLineException e) {
            System.out.println("\n  [!] Undo failed: " + e.getMessage());
        }
    }

    private void handleSalesHistory() {
        try {
            List<Sale> sales = salesService.getSalesHistory();
            System.out.println();
            System.out.println("╔═══════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.printf("║ SALES TRANSACTION LEDGER (%2d COMPLETED SALES) %52s ║\n", sales.size(), "");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════════════════════════╣");
            System.out.printf("║ %-6s │ %-7s │ %-20s │ %-20s │ %-12s │ %-16s ║\n",
                    "SaleID", "CarID", "Buyer", "Seller", "Date", "Final Price ($)");
            System.out.println("╟───────┼─────────┼──────────────────────┼──────────────────────┼──────────────┼──────────────────╢");

            for (Sale s : sales) {
                System.out.printf("║ #%-5d │ #%-6d │ %-20s │ %-20s │ %-12s │ $%,-15.2f ║\n",
                        s.getSaleId(), s.getCarId(), s.getBuyerName(), s.getSellerName(), s.getSaleDate(), s.getPrice());
            }
            System.out.println("╚═══════╧═════════╧══════════════════════╧══════════════════════╧══════════════╧══════════════════╝");

            if (!sales.isEmpty() && reader.confirm("  Reprint receipt for a sale?")) {
                int saleId = reader.readInt("  Enter Sale ID: ", 1, 999999);
                Sale target = salesService.getSaleById(saleId);
                if (target != null) {
                    Car c = null;
                    try { c = inventoryService.findById(target.getCarId()); } catch (Exception ignored) {}
                    System.out.print(salesService.generateReceipt(target, c));
                } else {
                    System.out.println("  [!] Sale ID #" + saleId + " not found.");
                }
            }

        } catch (RepositoryException e) {
            System.out.println("\n  [!] Failed to load sales history: " + e.getMessage());
        }
    }

    private void handleShowroomSummary() {
        int total = inventoryService.count();
        int available = inventoryService.countAvailable();
        int sold = total - available;
        double stockVal = inventoryService.calculateTotalStockValue();
        double portfolioVal = inventoryService.calculateTotalPortfolioValue();

        Set<String> brands = inventoryService.getBrandsSorted();

        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ SHOWROOM VALUATION & PORTFOLIO SUMMARY                             ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Total Fleet Count      : %-40d ║\n", total);
        System.out.printf("║  Available Showroom Cars: %-40d ║\n", available);
        System.out.printf("║  Sold / Archived Cars   : %-40d ║\n", sold);
        System.out.println("╟────────────────────────────────────────────────────────────────────╢");
        System.out.printf("║  Active Stock Valuation : $%,-39.2f ║\n", stockVal);
        System.out.printf("║  Total Portfolio Value  : $%,-39.2f ║\n", portfolioVal);
        System.out.println("╟────────────────────────────────────────────────────────────────────╢");
        System.out.printf("║  Represented Brands (%2d): %-40s ║\n", brands.size(), String.join(", ", brands));
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
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
