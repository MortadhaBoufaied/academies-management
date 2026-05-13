package com.footballacademy.services.payment;

import com.footballacademy.model.Payment;
import com.footballacademy.model.PaymentTransaction;
import com.footballacademy.repository.PaymentRepository;
import com.footballacademy.repository.PaymentTransactionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public
class OnlinePaymentService {
    private final StripePaymentService stripePaymentService;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    public OnlinePaymentService(StripePaymentService stripePaymentService, PaymentRepository paymentRepository, PaymentTransactionRepository paymentTransactionRepository) {
        this.stripePaymentService = stripePaymentService;
        this.paymentRepository = paymentRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }
    /**      * Create payment intent for an existing payment      */
    public Map<String, Object> createPaymentIntentForPayment(Long paymentId) throws StripeException {
        Payment payment = paymentRepository.findById(paymentId) .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + paymentId));
        // Create metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("paymentId", paymentId.toString());
        metadata.put("playerId", payment.getPlayer() != null ? payment.getPlayer() .getId() .toString() : "");
        metadata.put("paymentType", payment.getType());
        // Create payment intent
        Map<String, Object> stripeResponse = stripePaymentService.createPaymentIntentWithMetadata(payment.getAmount(), payment.getCurrency(), "Payment for " + payment.getType(), metadata);
        // Create payment transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setStripePaymentIntentId((String) stripeResponse.get("paymentIntentId"));
        transaction.setPayment(payment);
        transaction.setAmount(payment.getAmount());
        transaction.setCurrency(payment.getCurrency());
        transaction.setStatus("PENDING");
        paymentTransactionRepository.save(transaction);
        return stripeResponse;
    }
    /**      * Process payment confirmation      */
    public PaymentTransaction processPaymentConfirmation(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = stripePaymentService.getPaymentIntent(paymentIntentId);
        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository .findByStripePaymentIntentId(paymentIntentId);
        if (transactionOpt.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found for payment intent: " + paymentIntentId);
        } PaymentTransaction transaction = transactionOpt.get();
        transaction.setStatus(paymentIntent.getStatus() .toUpperCase());
        if ("succeeded" .equals(paymentIntent.getStatus())) {
            transaction.setCompletedAt(LocalDateTime.now());
            // Update the original payment status
            Payment payment = transaction.getPayment();
            if (payment != null) {
                payment.setStatus("PAID");
                paymentRepository.save(payment);
            }
        } else
        if ("requires_payment_method" .equals(paymentIntent.getStatus())) {
            // Payment failed or requires action
            transaction.setFailureReason("Payment requires payment method");
        } else
        if ("canceled" .equals(paymentIntent.getStatus())) {
            transaction.setFailureReason("Payment was canceled");
        } return paymentTransactionRepository.save(transaction);
    }
    /**      * Cancel payment      */
    public PaymentTransaction cancelPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = stripePaymentService.cancelPaymentIntent(paymentIntentId);
        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository .findByStripePaymentIntentId(paymentIntentId);
        if (transactionOpt.isPresent()) {
            PaymentTransaction transaction = transactionOpt.get();
            transaction.setStatus("CANCELLED");
            transaction.setFailureReason("Payment canceled by user");
            transaction.setCompletedAt(LocalDateTime.now());
            return paymentTransactionRepository.save(transaction);
        } throw new IllegalArgumentException("Transaction not found for payment intent: " + paymentIntentId);
    }
    /**      * Get payment transaction by payment intent ID      */
    public Optional<PaymentTransaction> getTransactionByPaymentIntentId(String paymentIntentId) {
        return paymentTransactionRepository.findByStripePaymentIntentId(paymentIntentId);
    }
    /**      * Get all transactions for a payment      */
    public java.util.List<PaymentTransaction> getTransactionsByPaymentId(Long paymentId) {
        return paymentTransactionRepository.findByPaymentId(paymentId);
    }
    /**      * Check payment status from Stripe      */
    public Map<String, Object> checkPaymentStatus(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = stripePaymentService.getPaymentIntent(paymentIntentId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", paymentIntent.getStatus());
        response.put("amount", paymentIntent.getAmount());
        response.put("currency", paymentIntent.getCurrency());
        response.put("created", paymentIntent.getCreated());
        response.put("metadata", paymentIntent.getMetadata());
        return response;
    }
}
