package com.revline.util.exception;

/**
 * Checked exception thrown when a data line (CSV or script) is malformed.
 */
public class DataFormatException extends RevLineException {

    private static final long serialVersionUID = 1L;

    private final int lineNumber;

    public DataFormatException(String message, int lineNumber) {
        super("Line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }

    public DataFormatException(String message, int lineNumber, Throwable cause) {
        super("Line " + lineNumber + ": " + message, cause);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
