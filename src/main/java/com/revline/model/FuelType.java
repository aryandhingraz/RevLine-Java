package com.revline.model;

import com.revline.util.exception.InvalidCarException;

/**
 * Supported propulsion fuel types for vehicles in RevLine.
 */
public enum FuelType {
    PETROL("Petrol"),
    HYBRID("Hybrid"),
    ELECTRIC("Electric");

    private final String label;

    FuelType(String label) {
        this.label = label;
    }

    /**
     * Human-friendly display label (e.g. "Petrol").
     */
    public String label() {
        return label;
    }

    /**
     * Parses a string to FuelType case-insensitively.
     *
     * @param text input string
     * @return matching FuelType
     * @throws InvalidCarException if text does not match any FuelType
     */
    public static FuelType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidCarException("Fuel type cannot be empty.");
        }
        String clean = text.trim();
        for (FuelType ft : values()) {
            if (ft.name().equalsIgnoreCase(clean) || ft.label.equalsIgnoreCase(clean)) {
                return ft;
            }
        }
        throw new InvalidCarException("Unknown fuel type: '" + text + "'. Allowed: PETROL, HYBRID, ELECTRIC.");
    }
}
