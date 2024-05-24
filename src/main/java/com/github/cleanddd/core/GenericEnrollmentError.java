package com.github.cleanddd.core;

public class GenericEnrollmentError extends RuntimeException {
    public GenericEnrollmentError(String message) {
        super(message);
    }

    public GenericEnrollmentError(String message, Throwable cause) {
        super(message, cause);
    }
}
