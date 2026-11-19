package com.revline.util.exception;

/**
 * Base checked exception for all RevLine business errors.
 */
public class RevLineException extends Exception {

    private static final long serialVersionUID = 1L;

    public RevLineException(String message) {
        super(message);
    }

    public RevLineException(String message, Throwable cause) {
        super(message, cause);
    }
}
