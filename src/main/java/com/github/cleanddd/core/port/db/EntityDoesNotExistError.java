package com.github.cleanddd.core.port.db;

public class EntityDoesNotExistError extends PersistenceError {
    public EntityDoesNotExistError(String message) {
        super(message);
    }
}
