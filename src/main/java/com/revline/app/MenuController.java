package com.revline.app;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Status;
import com.revline.service.InventoryService;
import com.revline.service.SearchService;
import com.revline.ui.ConsoleTable;
import com.revline.ui.InputReader;
import com.revline.ui.MenuPrinter;
import com.revline.util.exception.CarNotFoundException;
import com.revline.util.exception.InvalidCarException;
import com.revline.util.exception.RepositoryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Controller orchestrating console interactions for the initial prototype.
 */
public class MenuController {

    private final InventoryService inventoryService;
    private final SearchService searchService;
    private final InputReader reader;

    public MenuController(InventoryService inventoryService, SearchService searchService, InputReader reader) {
        this.inventoryService = inventoryService;
        this.searchService = searchService;
        this.reader = reader;
    }

    /**
     * Main interactive command loop.
     */
    public void run() {
        boolean running = true;
        while (running) {
            MenuPrinter.printMainMenu();
            int choice = reader.readInt("   Select an option (0-5): ", 0, 5);

            switch (choice) {
                case 1:
                    handleDisplayAll();
                    break;
                case 2:
                    handleAddCar();
                    break;
                case 3:
                    handleSearch();
                    break;
                case 4:
                    handleViewBrands();
                    break;
                case 5:
                    handleShowroomSummary();
                    break;
                case 0:
                    running = false;
                    System.out.println("\n  Exiting RevLine System... Thank you for driving passion. Goodbye!\n");
                    break;
                default:
                    System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    private void handleDisplayAll() {
        List<Car> all = inventoryService.findAll();
        ConsoleTable.render("DATA: SHOWROOM INVENTORY (" + all.size() + " VEHICLES)", all);
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
        } catch (InvalidCarException e) {
            System.out.println("\n  [!] Validation Error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.out.println("\n  [!] Persistence Failure: " + e.getMessage());
        }
    }

    private void handleSearch() {
        MenuPrinter.printSearchMenu();
        int subChoice = reader.readInt("   Select search method (0-2): ", 0, 2);

        if (subChoice == 1) {
            int id = reader.readInt("  Enter Car ID to look up: ", 1, 100000);
            try {
                Car car = searchService.findById(id);
                List<Car> single = new ArrayList<>();
                single.add(car);
                ConsoleTable.render("DATA: SEARCH RESULT FOR CAR #" + id, single);
            } catch (CarNotFoundException e) {
                System.out.println("\n  [!] " + e.getMessage());
            }
        } else if (subChoice == 2) {
            String query = reader.readLine("  Enter search query (brand, model, or fuel): ");
            try {
                List<Car> results = searchService.search(query);
                ConsoleTable.render("DATA: SEARCH RESULTS FOR '" + query + "'", results);
            } catch (UnsupportedOperationException e) {
                System.out.println();
                System.out.println("  ╔════════════════════════════════════════════════════════════════════╗");
                System.out.println("  ║ STUDENT EXERCISE PENDING: EX-3 (SearchService.search)              ║");
                System.out.println("  ║ This method is owned by the student and awaiting implementation.   ║");
                System.out.println("  ║ Hint & instructions: see docs/learning/EX3_HINT.md                 ║");
                System.out.println("  ╚════════════════════════════════════════════════════════════════════╝");
            }
        }
    }

    private void handleViewBrands() {
        Set<String> brands = inventoryService.getBrandsSorted();
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ BRAND DIRECTORY (SORTED ALPHABETICALLY)                            ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        int index = 1;
        for (String brand : brands) {
            System.out.printf("║  [%2d] %-59s ║\n", index++, brand);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }

    private void handleShowroomSummary() {
        int total = inventoryService.count();
        int available = inventoryService.countAvailable();
        double stockVal = inventoryService.calculateTotalStockValue();
        double portfolioVal = inventoryService.calculateTotalPortfolioValue();

        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ SHOWROOM PORTFOLIO SUMMARY                                         ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Total Vehicles in Registry : %-36d ║\n", total);
        System.out.printf("║  Available for Purchase     : %-36d ║\n", available);
        System.out.printf("║  Sold / Archived            : %-36d ║\n", (total - available));
        System.out.printf("║  Active Stock Value         : $%-35s ║\n", String.format("%,.2f", stockVal));
        System.out.printf("║  Total Portfolio Value      : $%-35s ║\n", String.format("%,.2f", portfolioVal));
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }
}
