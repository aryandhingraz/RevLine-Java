package com.revline.util.exception;

/**
 * Thrown when the standard console input stream is closed (EOF / Ctrl+Z / Ctrl+D).
 * Catches graceful termination across all interactive console loops (L3, L4).
 */
public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputClosedException() {
        super("Console input stream was closed (end of input reached).");
    }

    public InputClosedException(String message) {
        super(message);
    }
}
