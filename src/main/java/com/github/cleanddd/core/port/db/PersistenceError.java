package com.github.cleanddd.core.port.db;

import com.github.cleanddd.core.GenericEnrollmentError;

public class PersistenceError extends GenericEnrollmentError {
    public PersistenceError(String message) {
        super(message);
    }

    public PersistenceError(String message, Throwable cause) {
        super(message, cause);
    }
}
