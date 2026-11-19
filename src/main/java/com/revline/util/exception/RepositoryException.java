package com.revline.util.exception;

/**
 * Checked exception thrown when an underlying storage error occurs
 * in either file or JDBC operations.
 */
public class RepositoryException extends RevLineException {

    private static final long serialVersionUID = 1L;

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
