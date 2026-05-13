package com.footballacademy.exception;

import org.springframework.http.HttpStatus;

/**  * Base exception for application-specific errors  */
public
class ApplicationException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    public ApplicationException(String message) {
        super(message);
        this.errorCode = "APP_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "APP_ERROR";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    public ApplicationException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    public ApplicationException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    public String getErrorCode() {
        return errorCode;
    }
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
