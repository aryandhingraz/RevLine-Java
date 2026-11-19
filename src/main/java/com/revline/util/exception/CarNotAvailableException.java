package com.revline.util.exception;

/**
 * Checked exception thrown when an operation cannot be performed on a car
 * due to its current state (e.g. attempting to sell an already SOLD car).
 */
public class CarNotAvailableException extends RevLineException {

    private static final long serialVersionUID = 1L;

    public CarNotAvailableException(String message) {
        super(message);
    }
}
