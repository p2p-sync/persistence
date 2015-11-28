package org.rmatil.sync.persistence.exceptions;

/**
 * This exception is thrown if an error occurred during
 * writing, reading, deleting in the various implementations
 * of storage adapters.
 *
 * @see org.rmatil.sync.persistence.api.IStorageAdapter
 */
public class InputOutputException extends Exception {

    public InputOutputException() {
        super();
    }

    public InputOutputException(String message) {
        super(message);
    }

    public InputOutputException(String message, Throwable cause) {
        super(message, cause);
    }

    public InputOutputException(Throwable cause) {
        super(cause);
    }
}
