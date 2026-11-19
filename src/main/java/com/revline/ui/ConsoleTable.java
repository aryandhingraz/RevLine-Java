package com.revline.ui;

import com.revline.model.Car;
import com.revline.model.Status;

import java.util.List;

/**
 * Renders box-drawing supercar inventory tables matching the C prototype visual identity.
 */
public final class ConsoleTable {

    private ConsoleTable() {}

    public static void printTableTop(String title) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║ %-100s ║\n", title);
        System.out.println("╠════╦═════════════════╦═══════════════════╦═════════════╦═══════╦══════╦═══════╦════════════╦═════════╣");
        System.out.println("║ ID ║ Brand           ║ Model             ║   Price ($) ║ Speed ║  HP  ║ Accel ║ Fuel Type  ║ Status  ║");
        System.out.println("╠════╬═════════════════╬═══════════════════╬═════════════╬═══════╬══════╬═══════╬════════════╬═════════╣");
    }

    public static void printTableRow(Car car) {
        String priceStr = String.format("$%,.0f", car.getPrice());
        String statusStr = (car.getStatus() == Status.AVAILABLE) ? "Available" :
                           (car.getStatus() == Status.RESERVED) ? "Reserved" : "Sold";

        System.out.printf("║ %-2d ║ %-15s ║ %-17s ║ %11s ║ %-5d ║ %-4d ║ %-5.1f ║ %-10s ║ %-7s ║\n",
                car.getId(),
                truncate(car.getBrand(), 15),
                truncate(car.getModel(), 17),
                priceStr,
                car.getTopSpeed(),
                car.getHorsepower(),
                car.getAcceleration(),
                car.getFuelType().label(),
                statusStr);
    }

    public static void printTableBottom(int totalCount, int availableCount, double stockValue) {
        System.out.println("╚════╩═════════════════╩═══════════════════╩═════════════╩═══════╩══════╩═══════╩════════════╩═════════╝");
        if (totalCount >= 0) {
            String footer = String.format("FOOTER: Total Cars: %d  |  Available: %d  |  Stock Value (Available): $%,.2f",
                    totalCount, availableCount, stockValue);
            System.out.println("╔══════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.printf("║ %-100s ║\n", footer);
            System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════════════════════╝");
        }
    }

    public static void render(String title, List<Car> cars) {
        if (cars == null || cars.isEmpty()) {
            System.out.println();
            System.out.println("╔══════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.printf("║ %-100s ║\n", "No vehicles found matching criteria.");
            System.out.println("╚══════════════════════════════════════════════════════════════════════════════════════════════════════╝");
            return;
        }

        printTableTop(title);
        int available = 0;
        double stockValue = 0.0;

        for (Car c : cars) {
            printTableRow(c);
            if (c.getStatus() == Status.AVAILABLE) {
                available++;
                stockValue += c.getPrice();
            }
        }
        printTableBottom(cars.size(), available, stockValue);
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 1) + ".";
    }
}
