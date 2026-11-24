package com.revline.app;

import com.revline.model.Car;
import com.revline.model.FuelType;
import com.revline.model.Sale;
import com.revline.model.Status;
import com.revline.service.InventoryService;
import com.revline.service.SalesService;
import com.revline.ui.ConsoleTable;
import com.revline.ui.InputReader;
import com.revline.util.exception.CarNotFoundException;
import com.revline.util.exception.InvalidCarException;
import com.revline.util.exception.RepositoryException;
import com.revline.util.exception.RevLineException;

import java.util.List;

/**
 * Handles showroom manager/seller console interactions (CRUD, undo, sales ledger).
 */
final class SellerMenuHandler {

    private final InventoryService inventoryService;
    private final SalesService salesService;
    private final InputReader reader;

    SellerMenuHandler(InventoryService inventoryService, SalesService salesService, InputReader reader) {
        this.inventoryService = inventoryService;
        this.salesService = salesService;
        this.reader = reader;
    }

    void handleDisplayAllInventory() {
        List<Car> all = inventoryService.findAll();
        ConsoleTable.render("DATA: COMPLETE SHOWROOM INVENTORY (" + all.size() + " VEHICLES)", all);
    }

    void handleAddCar() {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║ ADD A NEW SUPERCAR                                                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");

        try {
            Car newCar = readNewCarInput();
            Car saved = inventoryService.addCar(newCar);
            printAddCarSuccess(saved);
        } catch (InvalidCarException | RepositoryException e) {
            System.out.println("\n  [!] Failed to add vehicle: " + e.getMessage());
        }
    }

    private Car readNewCarInput() {
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

        return new Car(0, brand, model, price, speed, fuelType, hp, accel, Status.AVAILABLE);
    }

    private void printAddCarSuccess(Car saved) {
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║ SUCCESS: '%s %s' added to showroom! %-24s ║\n", saved.getBrand(), saved.getModel(), "");
        System.out.printf("║ Assigned Car ID: %-50d ║\n", saved.getId());
        System.out.println("╚════════════════════════════════════════════════════════════════════╝");
    }

    void handleUpdateCar() {
        System.out.println("\n--- UPDATE SUPERCAR SPECIFICATIONS ---");
        int id = reader.readInt("  Enter Car ID to update: ", 1, 999999);

        try {
            Car existing = inventoryService.findById(id);
            if (existing.getStatus() != Status.AVAILABLE) {
                System.out.println("  [!] Cannot update vehicle with status " + existing.getStatus() + "; must be AVAILABLE.");
                return;
            }

            displayCarSpecs(existing);
            Car updated = readUpdatedCar(id, existing);
            inventoryService.updateCar(id, updated);

            System.out.println("\n  [✓] Vehicle #" + id + " specifications updated successfully.");
        } catch (RevLineException e) {
            System.out.println("\n  [!] Update failed: " + e.getMessage());
        }
    }

    private void displayCarSpecs(Car c) {
        System.out.printf("  Current: %s %s | $%,.2f | %d km/h | %d HP | %.2f s | %s\n",
                c.getBrand(), c.getModel(), c.getPrice(), c.getTopSpeed(),
                c.getHorsepower(), c.getAcceleration(), c.getFuelType());
    }

    private Car readUpdatedCar(int id, Car existing) {
        String brand = reader.readOptionalLine("  Enter New Brand [" + existing.getBrand() + "]: ", existing.getBrand());
        String model = reader.readOptionalLine("  Enter New Model [" + existing.getModel() + "]: ", existing.getModel());
        double price = reader.readOptionalDouble("  Enter New Price ($) [" + String.format("%.2f", existing.getPrice()) + "]: ",
                existing.getPrice(), 1000.0, 50000000.0);
        int speed = reader.readOptionalInt("  Enter New Top Speed (km/h) [" + existing.getTopSpeed() + "]: ",
                existing.getTopSpeed(), 50, 600);

        System.out.println("  Fuel Types: [1] PETROL  [2] HYBRID  [3] ELECTRIC");
        int defaultFuelChoice = (existing.getFuelType() == FuelType.PETROL) ? 1 :
                (existing.getFuelType() == FuelType.HYBRID) ? 2 : 3;
        int fuelChoice = reader.readOptionalInt("  Select Fuel Type (1-3) [" + defaultFuelChoice + "]: ", defaultFuelChoice, 1, 3);
        FuelType fuelType = (fuelChoice == 1) ? FuelType.PETROL :
                (fuelChoice == 2) ? FuelType.HYBRID : FuelType.ELECTRIC;

        int hp = reader.readOptionalInt("  Enter New Horsepower [" + existing.getHorsepower() + "]: ",
                existing.getHorsepower(), 50, 3000);
        double accel = reader.readOptionalDouble("  Enter New 0-100 Acceleration [" + existing.getAcceleration() + "]: ",
                existing.getAcceleration(), 1.0, 20.0);

        return new Car(id, brand, model, price, speed, fuelType, hp, accel, existing.getStatus());
    }

    void handleDeleteCar() {
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

    void handleUndo() {
        System.out.println("\n--- UNDO LAST INVENTORY OPERATION ---");
        try {
            String message = inventoryService.getUndoManager().undo(inventoryService);
            System.out.println("\n  [✓] " + message);
        } catch (RevLineException e) {
            System.out.println("\n  [!] Undo failed: " + e.getMessage());
        }
    }

    void handleSalesHistory() {
        try {
            List<Sale> sales = salesService.getSalesHistory();
            printSalesLedger(sales);
            if (!sales.isEmpty()) {
                promptReceiptReprint();
            }
        } catch (RepositoryException e) {
            System.out.println("\n  [!] Failed to load sales history: " + e.getMessage());
        }
    }

    private void printSalesLedger(List<Sale> sales) {
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
    }

    private void promptReceiptReprint() {
        if (!reader.confirm("  Reprint receipt for a sale?")) return;
        int saleId = reader.readInt("  Enter Sale ID: ", 1, 999999);
        try {
            Sale target = salesService.getSaleById(saleId);
            if (target != null) {
                Car c = null;
                try {
                    c = inventoryService.findById(target.getCarId());
                } catch (CarNotFoundException e) {
                    // Vehicle may have been removed or archived
                }
                System.out.print(salesService.generateReceipt(target, c));
            } else {
                System.out.println("  [!] Sale ID #" + saleId + " not found.");
            }
        } catch (RepositoryException e) {
            System.out.println("  [!] Error retrieving sale: " + e.getMessage());
        }
    }
}
