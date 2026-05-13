package com.footballacademy.services.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public
class StripePaymentService {
    @Value("${stripe.secret.key:}")
    private String stripeSecretKey;
    public void initializeStripe() {
        if (stripeSecretKey == null || stripeSecretKey.trim() .isEmpty()) {
            throw new IllegalStateException("Stripe secret key is not configured. Please set stripe.secret.key");
        } Stripe.apiKey = stripeSecretKey.trim();
    }
    public Map<String, Object> createPaymentIntent(Double amount, String currency, String description) throws StripeException {
        initializeStripe();
        PaymentIntentCreateParams params = baseCreateParams(amount, currency, description) .build();
        return toPaymentIntentResponse(PaymentIntent.create(params));
    }
    public Map<String, Object> createPaymentIntentWithMetadata(Double amount, String currency, String description, Map<String, String> metadata) throws StripeException {
        initializeStripe();
        PaymentIntentCreateParams.Builder builder = baseCreateParams(amount, currency, description);
        if (metadata != null && !metadata.isEmpty()) {
            builder.putAllMetadata(metadata);
        } return toPaymentIntentResponse(PaymentIntent.create(builder.build()));
    }
    public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
        initializeStripe();
        PaymentIntent paymentIntent = PaymentIntent.retrieve(validatePaymentIntentId(paymentIntentId));
        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder() .build();
        return paymentIntent.confirm(params);
    }
    public PaymentIntent getPaymentIntent(String paymentIntentId) throws StripeException {
        initializeStripe();
        return PaymentIntent.retrieve(validatePaymentIntentId(paymentIntentId));
    }
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        initializeStripe();
        PaymentIntent paymentIntent = PaymentIntent.retrieve(validatePaymentIntentId(paymentIntentId));
        PaymentIntentCancelParams params = PaymentIntentCancelParams.builder() .build();
        return paymentIntent.cancel(params);
    }
    public Map<String, Object> getPaymentIntentStatus(String paymentIntentId) throws StripeException {
        return toPaymentIntentResponse(getPaymentIntent(paymentIntentId));
    }
    private PaymentIntentCreateParams.Builder baseCreateParams(Double amount, String currency, String description) {
        return PaymentIntentCreateParams.builder() .setAmount(convertAmountToSmallestCurrencyUnit(amount)) .setCurrency(normalizeCurrency(currency)) .setDescription(safeDescription(description)) .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder() .setEnabled(true) .build());
    }
    private Map<String, Object> toPaymentIntentResponse(PaymentIntent paymentIntent) {
        Map<String, Object> response = new HashMap<>();
        response.put("clientSecret", paymentIntent.getClientSecret());
        response.put("paymentIntentId", paymentIntent.getId());
        response.put("amount", paymentIntent.getAmount());
        response.put("currency", paymentIntent.getCurrency());
        response.put("status", paymentIntent.getStatus());
        response.put("created", paymentIntent.getCreated());
        response.put("metadata", paymentIntent.getMetadata());
        return response;
    }
    private long convertAmountToSmallestCurrencyUnit(Double amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Payment amount is required");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        } return BigDecimal.valueOf(amount) .multiply(BigDecimal.valueOf(100)) .setScale(0, RoundingMode.HALF_UP) .longValueExact();
    }
    private String normalizeCurrency(String currency) {
        if (currency == null || currency.trim() .isEmpty()) {
            throw new IllegalArgumentException("Payment currency is required");
        } return currency.trim() .toLowerCase(Locale.ROOT);
    }
    private String safeDescription(String description) {
        if (description == null || description.trim() .isEmpty()) {
            return "Football Academy Payment";
        } return description.trim();
    }
    private String validatePaymentIntentId(String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.trim() .isEmpty()) {
            throw new IllegalArgumentException("Payment intent id is required");
        } return paymentIntentId.trim();
    }
}
