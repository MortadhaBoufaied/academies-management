package com.footballacademy.exception;

import org.springframework.http.HttpStatus;

/**  * Exception thrown when validation fails  */
public
class ValidationException extends ApplicationException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
    }
    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", String.format("Validation failed for field '%s': %s", field, message), HttpStatus.BAD_REQUEST);
    }
}
