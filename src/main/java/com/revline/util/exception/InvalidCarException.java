package com.revline.util.exception;

/**
 * Unchecked exception thrown by constructors and validators when invalid
 * domain data is supplied (e.g. negative price, blank brand).
 */
public class InvalidCarException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidCarException(String message) {
        super(message);
    }

    public InvalidCarException(String message, Throwable cause) {
        super(message, cause);
    }
}
