package com.revline.app;

import com.revline.model.Car;
import com.revline.service.InventoryService;
import com.revline.service.RecentlyViewed;
import com.revline.ui.InputReader;

import java.util.List;
import java.util.ListIterator;

/**
 * Interactive showroom vehicle browser using bidirectional ListIterator (L6, L7).
 * Demonstrates cursor positioning rules where the iterator cursor sits between elements.
 */
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

        printBrowserHeader();
        ListIterator<Car> iterator = cars.listIterator();
        while (iterator.hasNext()) {
            Car current = iterator.next();
            recentlyViewed.recordView(current);
            displayCarDetailCard(current, iterator.nextIndex(), cars.size());

            boolean shouldAdvance = promptNextAction(iterator, cars);
            if (!shouldAdvance) {
                return;
            }
        }
    }

    private void printBrowserHeader() {
        System.out.println("\n--- INTERACTIVE SHOWROOM BROWSER (L6-7 ListIterator) ---");
        System.out.println("Commands: [N] Next, [P] Previous, [Q] Quit browsing");
    }

    private boolean promptNextAction(ListIterator<Car> iterator, List<Car> cars) {
        while (true) {
            String input = reader.readCommand("  [N]ext / [P]revious / [Q]uit: ");
            if (input.equalsIgnoreCase("N")) {
                if (!iterator.hasNext()) {
                    System.out.println("  [i] You are at the end of the showroom.");
                } else {
                    return true;
                }
            } else if (input.equalsIgnoreCase("P")) {
                handlePrevious(iterator, cars);
            } else if (input.equalsIgnoreCase("Q")) {
                return false;
            } else {
                System.out.println("  [!] Please enter N, P, or Q.");
            }
        }
    }

    /**
     * Handles previous vehicle navigation using ListIterator cursor mechanics (L6-7).
     *
     * Cursor Rule: The ListIterator cursor sits BETWEEN elements, not ON an element:
     *   Positions:     ^ (0)   [Car 0]   ^ (1)   [Car 1]   ^ (2)
     * When Car 1 is currently displayed, iterator.next() has placed the cursor at position (2).
     * 1. iterator.previous() moves cursor to (1) and returns Car 1 (the currently displayed car).
     * 2. If a prior vehicle exists, iterator.previous() moves cursor to (0) and returns Car 0.
     * 3. iterator.next() moves cursor back to (1) so the next forward step properly accesses Car 1.
     */
    private void handlePrevious(ListIterator<Car> iterator, List<Car> cars) {
        if (!iterator.hasPrevious()) {
            System.out.println("  [i] You are at the first vehicle.");
            return;
        }

        // Step past the currently displayed car
        iterator.previous();

        if (iterator.hasPrevious()) {
            // Retrieve and display the prior car
            Car prior = iterator.previous();
            recentlyViewed.recordView(prior);
            // Reposition cursor immediately after prior car so forward iteration stays aligned
            iterator.next();
            displayCarDetailCard(prior, iterator.nextIndex(), cars.size());
        } else {
            // Restore cursor position if already at head of list
            iterator.next();
            System.out.println("  [i] You are at the first vehicle.");
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
        System.out.printf("  |  Fuel Type    : %-46s |\n", c.getFuelType());
        System.out.printf("  |  Status       : %-46s |\n", c.getStatus());
        System.out.println("  +-------------------------------------------------------------+");
    }
}
