package com.revline.app;

import com.revline.model.Car;
import com.revline.service.InventoryService;
import com.revline.service.RecentlyViewed;
import com.revline.ui.InputReader;

import java.util.List;
import java.util.ListIterator;

/** Owns the interactive next/previous showroom browsing flow. */
final class BuyerShowroomBrowser {
    private final InventoryService inventoryService;
    private final RecentlyViewed recentlyViewed;
    private final InputReader reader;

    BuyerShowroomBrowser(InventoryService inventoryService, RecentlyViewed recentlyViewed, InputReader reader) {
        this.inventoryService = inventoryService;
        this.recentlyViewed = recentlyViewed;
        this.reader = reader;
    }

    void browse() {
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
                    if (!iterator.hasNext()) System.out.println("  [i] You are at the end of the showroom.");
                    else browsing = false;
                } else if (input.equalsIgnoreCase("P")) {
                    if (iterator.hasPrevious()) {
                        iterator.previous();
                        if (iterator.hasPrevious()) {
                            current = iterator.previous();
                            recentlyViewed.recordView(current);
                            iterator.next();
                            displayCarDetailCard(current, iterator.nextIndex(), cars.size());
                        } else {
                            iterator.next();
                            System.out.println("  [i] You are at the first vehicle.");
                        }
                    }
                } else if (input.equalsIgnoreCase("Q")) return;
                else System.out.println("  [!] Please enter N, P, or Q.");
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
}
