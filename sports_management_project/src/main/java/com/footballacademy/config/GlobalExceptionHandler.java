package com.footballacademy.config;

import com.footballacademy.exception.ApplicationException;
import com.footballacademy.exception.BusinessException;
import com.footballacademy.exception.PaymentException;
import com.footballacademy.exception.ResourceNotFoundException;
import com.footballacademy.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_LOGGER");
    /**      * Handle authentication failures      */
    @ExceptionHandler({
        BadCredentialsException.class, AuthenticationException.class
    })
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(Exception e) {
        securityLogger.warn("Authentication failed for user: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Invalid email or password");
        body.put("path", "");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(body);
    }
    /**      * Handle validation errors      */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult() .getAllErrors() .forEach((error) -> {
            String fieldName =((FieldError) error) .getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.warn("Validation failed: {}", errors);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("message", "Input validation failed");
        body.put("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(body);
    }
    /**      * Handle illegal argument exceptions      */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e, WebRequest request) {
        logger.warn("Invalid argument: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(body);
    }
    /**      * Handle illegal state exceptions      */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException e, WebRequest request) {
        logger.error("Illegal state: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", "Operation cannot be completed due to current state");
        return ResponseEntity.status(HttpStatus.CONFLICT) .body(body);
    }
    /**      * Handle runtime exceptions      */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e, WebRequest request) {
        logger.error("Runtime error occurred: {}", e.getMessage(), e);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(body);
    }
    /**      * Handle generic exceptions      */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e, WebRequest request) {
        logger.error("Unexpected error occurred: {}", e.getMessage(), e);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(body);
    }
    /**      * Handle application-specific exceptions      */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Map<String, Object>> handleApplicationException(ApplicationException e, WebRequest request) {
        logger.warn("Application error [{}]: {}", e.getErrorCode(), e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", e.getHttpStatus() .value());
        body.put("error", e.getHttpStatus() .getReasonPhrase());
        body.put("errorCode", e.getErrorCode());
        body.put("message", e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()) .body(body);
    }
    /**      * Handle resource not found exceptions      */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        logger.warn("Resource not found: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("errorCode", e.getErrorCode());
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(body);
    }
    /**      * Handle validation exceptions      */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException e, WebRequest request) {
        logger.warn("Validation error: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("errorCode", e.getErrorCode());
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(body);
    }
    /**      * Handle business exceptions      */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException e, WebRequest request) {
        logger.warn("Business rule violation: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("error", "Business Rule Violation");
        body.put("errorCode", e.getErrorCode());
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY) .body(body);
    }
    /**      * Handle payment exceptions      */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentException(PaymentException e, WebRequest request) {
        logger.warn("Payment error: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Payment Error");
        body.put("errorCode", e.getErrorCode());
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(body);
    }
    /**      * Handle resource not found exceptions (JPA)      */
    @ExceptionHandler({
        jakarta.persistence.EntityNotFoundException.class, org.springframework.dao.EmptyResultDataAccessException.class
    })
    public ResponseEntity<Map<String, Object>> handleJpaNotFoundException(Exception e) {
        logger.warn("JPA resource not found: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", "Requested resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(body);
    }
    /**      * Handle data access exceptions      */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(org.springframework.dao.DataAccessException e) {
        logger.error("Data access error: {}", e.getMessage(), e);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Data Access Error");
        body.put("message", "Failed to access or process data");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(body);
    }
    /**      * Handle constraint violation exceptions      */
    @ExceptionHandler({
        org.springframework.dao.DataIntegrityViolationException.class, jakarta.validation.ConstraintViolationException.class
    })
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(Exception e) {
        logger.warn("Constraint violation: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Constraint Violation");
        // Provide more specific message for unique constraint violations
        if (e instanceof org.springframework.dao.DataIntegrityViolationException) {
            String message = e.getMessage();
            if (message != null && message.contains("uk_payment_player_month")) {
                body.put("message", "Payment already exists for this player and month");
            } else {
                body.put("message", "Data integrity constraint violated");
            }
        } else {
            body.put("message", "Data integrity constraint violated");
        } return ResponseEntity.status(HttpStatus.CONFLICT) .body(body);
    }
    /**      * Handle optimistic locking failures      */
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.
    class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailureException(org.springframework.orm.ObjectOptimisticLockingFailureException e) {
        logger.warn("Optimistic locking failure: {}", e.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Concurrent Modification");
        body.put("message", "This record was modified by another user. Please refresh and try again.");
        return ResponseEntity.status(HttpStatus.CONFLICT) .body(body);
    }
}
