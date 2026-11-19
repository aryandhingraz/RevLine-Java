package com.revline.util;

import com.revline.util.exception.InvalidCarException;

/**
 * Validates vehicle fields, input strings, and numbers (L2, L3, L4).
 */
public final class Validator {

    private Validator() {}

    /**
     * Ensures a string is non-null and not blank.
     */
    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidCarException(fieldName + " cannot be blank.");
        }
        return value.trim();
    }

    /**
     * Validates that brand and model names do not contain commas or newlines
     * to keep CSV parsing simple and safe.
     */
    public static void validateStringNoDelimiter(String value, String fieldName) {
        requireNonBlank(value, fieldName);
        if (value.contains(",") || value.contains("\n") || value.contains("\r")) {
            throw new InvalidCarException(fieldName + " cannot contain commas or newlines.");
        }
    }

    /**
     * Parses an integer safely, throwing InvalidCarException on failure.
     */
    public static int parsePositiveInt(String text, String fieldName) {
        requireNonBlank(text, fieldName);
        try {
            int val = Integer.parseInt(text.trim());
            if (val <= 0) {
                throw new InvalidCarException(fieldName + " must be a positive integer.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new InvalidCarException(fieldName + " must be a valid number, got: '" + text + "'.", e);
        }
    }

    /**
     * Validates speed in km/h.
     */
    public static void validateTopSpeed(int speed) {
        if (speed < 50 || speed > 600) {
            throw new InvalidCarException("Top speed must be between 50 and 600 km/h.");
        }
    }

    /**
     * Validates engine horsepower.
     */
    public static void validateHorsepower(int hp) {
        if (hp < 50 || hp > 3000) {
            throw new InvalidCarException("Horsepower must be between 50 and 3000 HP.");
        }
    }

    /**
     * Validates 0-100 acceleration time in seconds.
     */
    public static void validateAcceleration(double accel) {
        if (accel < 1.0 || accel > 20.0) {
            throw new InvalidCarException("Acceleration must be between 1.0 and 20.0 seconds.");
        }
    }

    // =========================================================================
    // STUDENT EXERCISE EX-1 ZONE
    // =========================================================================

    /**
     * EX-1 (Part A): Checks if a fuel type string is valid (case-insensitive).
     * Valid types: "PETROL", "HYBRID", "ELECTRIC" (or "Petrol", "Hybrid", "Electric").
     *
     * @param text input fuel string
     * @return true if valid, false if null, empty, or unrecognised
     * @see docs/learning/EX1_HINT.md
     */
    public static boolean isValidFuelType(String text) {
        // --- STUDENT EXERCISE EX-1 (Part A) ---
        // Aryan will write this implementation.
        throw new UnsupportedOperationException("Student exercise EX-1");
    }

    /**
     * EX-1 (Part B): Parses a positive double (e.g. price) from string.
     * Must trim whitespace, parse using Double.parseDouble(), and verify value > 0.0.
     *
     * @param text string containing a positive number
     * @param fieldName label for exception messages
     * @return parsed positive double
     * @throws InvalidCarException if text is blank, not a number, or <= 0.0
     * @see docs/learning/EX1_HINT.md
     */
    public static double parsePositiveDouble(String text, String fieldName) {
        // --- STUDENT EXERCISE EX-1 (Part B) ---
        // Aryan will write this implementation.
        throw new UnsupportedOperationException("Student exercise EX-1");
    }
}
