package com.github.cleanddd.core.port.db;

import com.github.cleanddd.core.GenericEnrollmentError;

public class EntityDoesNotExistError extends GenericEnrollmentError {
    public EntityDoesNotExistError(String message) {
        super(message);
    }
}
