package com.footballacademy.exception;

import org.springframework.http.HttpStatus;

/**  * Exception thrown when a business rule is violated  */
public
class BusinessException extends ApplicationException {
    public BusinessException(String message) {
        super("BUSINESS_ERROR", message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    public BusinessException(String errorCode, String message) {
        super(errorCode, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    public BusinessException(String message, Throwable cause) {
        super("BUSINESS_ERROR", message, HttpStatus.UNPROCESSABLE_ENTITY, cause);
    }
}
