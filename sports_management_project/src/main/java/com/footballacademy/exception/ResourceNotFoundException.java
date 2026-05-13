package com.footballacademy.exception;

import org.springframework.http.HttpStatus;

/**  * Exception thrown when a requested resource is not found  */
public
class ResourceNotFoundException extends ApplicationException {
    public ResourceNotFoundException(String resource, Long id) {
        super("RESOURCE_NOT_FOUND", String.format("%s not found with id: %d", resource, id), HttpStatus.NOT_FOUND);
    }
    public ResourceNotFoundException(String resource, String identifier) {
        super("RESOURCE_NOT_FOUND", String.format("%s not found: %s", resource, identifier), HttpStatus.NOT_FOUND);
    }
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
