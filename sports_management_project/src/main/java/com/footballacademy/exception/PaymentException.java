package com.footballacademy.exception;

import org.springframework.http.HttpStatus;

/**  * Exception thrown when payment processing fails  */
public
class PaymentException extends ApplicationException {
    public PaymentException(String message) {
        super("PAYMENT_ERROR", message, HttpStatus.BAD_REQUEST);
    }
    public PaymentException(String message, Throwable cause) {
        super("PAYMENT_ERROR", message, HttpStatus.BAD_REQUEST, cause);
    }
    public static PaymentException paymentNotFound(Long paymentId) {
        return new PaymentException(String.format("Payment not found with id: %d", paymentId));
    }
    public static PaymentException paymentFailed(String reason) {
        return new PaymentException(String.format("Payment processing failed: %s", reason));
    }
    public static PaymentException invalidAmount(Double amount) {
        return new PaymentException(String.format("Invalid payment amount: %.2f", amount));
    }
}
