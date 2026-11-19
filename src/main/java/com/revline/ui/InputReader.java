package com.revline.ui;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Exception-safe console input reader (L4).
 * Always reads whole lines to prevent scanner buffer misalignment bugs.
 */
public class InputReader {

    private final Scanner scanner;

    public InputReader() {
        this.scanner = new Scanner(System.in);
    }

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Reads a non-blank line of text from the user.
     */
    public String readLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                if (!scanner.hasNextLine()) {
                    return ""; // EOF encountered (e.g. scripted test)
                }
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    return line;
                }
                System.out.println("  [!] Input cannot be empty. Please enter a value.");
            } catch (NoSuchElementException e) {
                return "";
            }
        }
    }

    /**
     * Reads an integer within a specified range [min, max].
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                if (!scanner.hasNextLine()) {
                    return min;
                }
                String line = scanner.nextLine().trim();
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [!] Please enter a number between %d and %d.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a valid whole number.");
            } catch (NoSuchElementException e) {
                return min;
            }
        }
    }

    /**
     * Reads a decimal value within a specified range [min, max].
     */
    public double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                if (!scanner.hasNextLine()) {
                    return min;
                }
                String line = scanner.nextLine().trim();
                double value = Double.parseDouble(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [!] Please enter a value between %,.2f and %,.2f.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a valid decimal number.");
            } catch (NoSuchElementException e) {
                return min;
            }
        }
    }

    /**
     * Confirms a yes/no action with the user.
     */
    public boolean confirm(String prompt) {
        System.out.print(prompt + " (y/N): ");
        try {
            if (!scanner.hasNextLine()) {
                return false;
            }
            String line = scanner.nextLine().trim();
            return line.equalsIgnoreCase("y") || line.equalsIgnoreCase("yes");
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
