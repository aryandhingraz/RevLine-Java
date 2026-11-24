package com.revline.app;

import com.revline.model.Car;
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
import com.revline.util.exception.CarNotFoundException;
import com.revline.util.exception.RevLineException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Handles client/buyer console interactions (browsing, search, compare, reserve, buy).
 */
final class BuyerMenuHandler {

    private final InventoryService inventoryService;
    private final SearchService searchService;
    private final SalesService salesService;
    private final ComparisonService comparisonService;
    private final RecentlyViewed recentlyViewed;
    private final InputReader reader;
    private final BuyerShowroomBrowser showroomBrowser;

    BuyerMenuHandler(InventoryService inventoryService,
                     SearchService searchService,
                     SalesService salesService,
                     ComparisonService comparisonService,
                     RecentlyViewed recentlyViewed,
                     InputReader reader) {
        this.inventoryService = inventoryService;
        this.searchService = searchService;
        this.salesService = salesService;
        this.comparisonService = comparisonService;
        this.recentlyViewed = recentlyViewed;
        this.reader = reader;
        this.showroomBrowser = new BuyerShowroomBrowser(inventoryService, recentlyViewed, reader);
    }

    void handleCommand(String cmd) {
        switch (cmd) {
            case "1": handleBrowseShowroom(); break;
            case "2": handleSearchAndFilter(); break;
            case "3": handleSortShowroom(); break;
            case "4": handleCompareCars(); break;
            case "5": handleReserveCar(); break;
            case "6": handlePurchaseCar(); break;
            case "7": handleJoinWaitlist(); break;
            case "8": handleRecentlyViewed(); break;
            default: System.out.println("  [!] Invalid choice. Please enter 1-8, S, or 0.");
        }
    }

    private void handleBrowseShowroom() {
        showroomBrowser.browse();
    }

    private void handleSearchAndFilter() {
        MenuPrinter.printSearchFilterMenu();
        int choice = reader.readInt("   Select search/filter mode (0-5): ", 0, 5);
        if (choice == 0) return;

        List<Car> all = inventoryService.findAll();
        List<Car> results = new ArrayList<>();

        switch (choice) {
            case 1: searchById(results); break;
            case 2: searchByText(results); break;
            case 3: filterByBudget(all, results); break;
            case 4: filterByPerformance(all, results); break;
            case 5: results.addAll(new FilterBuilder().availableOnly().filter(all)); break;
            default: break;
        }
        ConsoleTable.render("SEARCH RESULTS (" + results.size() + " MATCHES)", results);
    }

    private void searchById(List<Car> results) {
        int id = reader.readInt("  Enter exact Car ID to find: ", 1, 999999);
        try {
            Car found = searchService.findById(id);
            results.add(found);
            recentlyViewed.recordView(found);
        } catch (CarNotFoundException e) {
            System.out.println("\n  [!] " + e.getMessage());
        }
    }

    private void searchByText(List<Car> results) {
        String query = reader.readLine("  Enter search text (e.g. 'Ferrari', 'Hybrid', '911'): ");
        List<Car> matches = searchService.search(query);
        results.addAll(matches);
        for (Car c : matches) {
            recentlyViewed.recordView(c);
        }
    }

    private void filterByBudget(List<Car> all, List<Car> results) {
        double min = reader.readDouble("  Enter Minimum Budget ($): ", 0.0, 50000000.0);
        double max = reader.readDouble("  Enter Maximum Budget ($): ", min, 50000000.0);
        results.addAll(new FilterBuilder().minPrice(min).maxPrice(max).filter(all));
    }

    private void filterByPerformance(List<Car> all, List<Car> results) {
        int minSpeed = reader.readInt("  Enter Minimum Top Speed (km/h): ", 0, 600);
        int minHp = reader.readInt("  Enter Minimum Horsepower (HP): ", 0, 3000);
        results.addAll(new FilterBuilder().minSpeed(minSpeed).minHorsepower(minHp).filter(all));
    }

    private void handleSortShowroom() {
        MenuPrinter.printSortMenu();
        int sortChoice = reader.readInt("   Select sort order (0-7): ", 0, 7);
        if (sortChoice == 0) return;

        Comparator<Car> comp;
        String title;

        switch (sortChoice) {
            case 1: comp = CarComparators.BY_PRICE_ASC; title = "SHOWROOM SORTED: PRICE (LOW TO HIGH)"; break;
            case 2: comp = CarComparators.BY_PRICE_DESC; title = "SHOWROOM SORTED: PRICE (HIGH TO LOW)"; break;
            case 3: comp = CarComparators.BY_SPEED_DESC; title = "SHOWROOM SORTED: TOP SPEED (FASTEST FIRST)"; break;
            case 4: comp = CarComparators.BY_HP_DESC; title = "SHOWROOM SORTED: HORSEPOWER (MOST POWERFUL FIRST)"; break;
            case 5: comp = CarComparators.BY_ACCEL_ASC; title = "SHOWROOM SORTED: ACCELERATION (0-100 QUICKEST FIRST)"; break;
            case 6: comp = CarComparators.BY_BRAND_MODEL; title = "SHOWROOM SORTED: BRAND & MODEL (ALPHABETICAL)"; break;
            case 7: comp = CarComparators.byValueForMoney(); title = "SHOWROOM SORTED: VALUE FOR MONEY (HP PER $100K)"; break;
            default: return;
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
                    if (selected.contains(c)) {
                        System.out.println("  [!] Car #" + id + " has already been selected for comparison. Please choose a different car.");
                        continue;
                    }
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
            String[] parties = promptSaleParties();
            if (!confirmPurchase(car)) {
                return;
            }

            Sale sale = salesService.sellCar(carId, parties[0], parties[1]);
            System.out.println();
            System.out.print(salesService.generateReceipt(sale, car));
        } catch (RevLineException e) {
            System.out.println("\n  [!] Purchase failed: " + e.getMessage());
        }
    }

    private String[] promptSaleParties() {
        String buyer = reader.readLine("  Enter Buyer Full Name: ");
        String seller = reader.readLine("  Enter Sales Representative Name (or RevLine): ");
        if (seller.trim().isEmpty()) {
            seller = "RevLine Showroom";
        }
        return new String[]{buyer, seller};
    }

    private boolean confirmPurchase(Car car) {
        boolean confirm = reader.confirm("  Confirm purchase for $" + String.format("%,.2f", car.getPrice()) + "?");
        if (!confirm) {
            System.out.println("  Purchase cancelled by user.");
            return false;
        }
        return true;
    }

    private void handleJoinWaitlist() {
        System.out.println("\n--- JOIN CUSTOMER WAITLIST (FIFO Priority) ---");
        int carId = reader.readInt("  Enter Car ID: ", 1, 999999);
        String buyer = reader.readLine("  Enter Your Full Name: ");

        try {
            Car car = inventoryService.findById(carId);
            if (car.getStatus() == Status.AVAILABLE) {
                System.out.printf("  [!] Car #%d is currently AVAILABLE. Waitlist is reserved for unavailable vehicles.\n", carId);
                return;
            }
            int pos = salesService.getWaitlistService().join(carId, buyer);
            System.out.printf("\n  [✓] %s added to waitlist for Car #%d! Current position in queue: #%d\n", buyer, carId, pos);
        } catch (CarNotFoundException e) {
            System.out.println("  [!] Car #" + carId + " not found in showroom inventory.");
        }
    }

    private void handleRecentlyViewed() {
        List<Car> viewed = recentlyViewed.getRecentlyViewedList();
        ConsoleTable.render("RECENTLY VIEWED SUPERCARS (LRU 5)", viewed);
    }
}
