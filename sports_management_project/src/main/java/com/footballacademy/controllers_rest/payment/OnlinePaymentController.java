package com.footballacademy.controllers_rest.payment;

import com.footballacademy.model.PaymentTransaction;
import com.footballacademy.services.payment.OnlinePaymentService;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/online")
public
class OnlinePaymentController {
    private final OnlinePaymentService onlinePaymentService;
    public OnlinePaymentController(OnlinePaymentService onlinePaymentService) {
        this.onlinePaymentService = onlinePaymentService;
    }
    /**      * Create payment intent for a payment      */
    @PostMapping("/create-intent/{paymentId}")
    public ResponseEntity<?> createPaymentIntent(
    @PathVariable Long paymentId) {
        try {
            Map<String, Object> response = onlinePaymentService.createPaymentIntentForPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        } catch (StripeException e) {
            return ResponseEntity.status(500) .body(Map.of("error", "Stripe error: " + e.getMessage()));
        }
    }
    /**      * Confirm payment (webhook endpoint)      */
    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<?> confirmPayment(
    @PathVariable String paymentIntentId) {
        try {
            PaymentTransaction transaction = onlinePaymentService.processPaymentConfirmation(paymentIntentId);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        } catch (StripeException e) {
            return ResponseEntity.status(500) .body(Map.of("error", "Stripe error: " + e.getMessage()));
        }
    }
    /**      * Cancel payment      */
    @PostMapping("/cancel/{paymentIntentId}")
    public ResponseEntity<?> cancelPayment(
    @PathVariable String paymentIntentId) {
        try {
            PaymentTransaction transaction = onlinePaymentService.cancelPayment(paymentIntentId);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        } catch (StripeException e) {
            return ResponseEntity.status(500) .body(Map.of("error", "Stripe error: " + e.getMessage()));
        }
    }
    /**      * Check payment status      */
    @GetMapping("/status/{paymentIntentId}")
    public ResponseEntity<?> checkPaymentStatus(
    @PathVariable String paymentIntentId) {
        try {
            Map<String, Object> status = onlinePaymentService.checkPaymentStatus(paymentIntentId);
            return ResponseEntity.ok(status);
        } catch (StripeException e) {
            return ResponseEntity.status(500) .body(Map.of("error", "Stripe error: " + e.getMessage()));
        }
    }
    /**      * Get transaction by payment intent ID      */
    @GetMapping("/transaction/{paymentIntentId}")
    public ResponseEntity<?> getTransaction(
    @PathVariable String paymentIntentId) {
        return onlinePaymentService.getTransactionByPaymentIntentId(paymentIntentId) .map(ResponseEntity::ok) .orElse(ResponseEntity.notFound() .build());
    }
    /**      * Get all transactions for a payment      */
    @GetMapping("/transactions/{paymentId}")
    public ResponseEntity<List<PaymentTransaction>> getPaymentTransactions(
    @PathVariable Long paymentId) {
        return ResponseEntity.ok(onlinePaymentService.getTransactionsByPaymentId(paymentId));
    }
}
