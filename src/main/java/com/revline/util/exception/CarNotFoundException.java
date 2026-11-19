package com.revline.util.exception;

/**
 * Checked exception thrown when a requested vehicle ID is not present in inventory.
 */
public class CarNotFoundException extends RevLineException {

    private static final long serialVersionUID = 1L;

    public CarNotFoundException(int carId) {
        super("Car with ID " + carId + " was not found.");
    }

    public CarNotFoundException(String message) {
        super(message);
    }
}
