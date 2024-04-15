package com.github.cleanddd.core.model;

import com.github.cleanddd.core.GenericEnrollmentError;

public class InvalidDomainEntityError extends GenericEnrollmentError {
    public InvalidDomainEntityError(String message) {
        super(message);
    }
}
