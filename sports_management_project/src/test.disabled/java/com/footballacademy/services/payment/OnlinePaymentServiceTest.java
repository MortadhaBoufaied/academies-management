package com.footballacademy.services.payment;

import com.footballacademy.model.Payment;
import com.footballacademy.model.PaymentTransaction;
import com.footballacademy.model.Player;
import com.footballacademy.model.User;
import com.footballacademy.repository.PaymentRepository;
import com.footballacademy.repository.PaymentTransactionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.
class)
class OnlinePaymentServiceTest {
    @Mock
    private StripePaymentService stripePaymentService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;
    @InjectMocks
    private OnlinePaymentService onlinePaymentService;
    private Payment payment;
    private Player player;
    private User user;
    @BeforeEach void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        player = new Player();
        player.setId(1L);
        player.setUser(user);
        payment = new Payment();
        payment.setId(1L);
        payment.setPlayer(player);
        payment.setAmount(100.0);
        payment.setCurrency("USD");
        payment.setStatus("PENDING");
    }
    @Test void createPaymentIntentForPayment_WhenValid_ShouldReturnPaymentIntent() throws StripeException {
        java.util.Map<String, Object> stripeResponse = new java.util.HashMap<>();
        stripeResponse.put("clientSecret", "pi_test_secret");
        stripeResponse.put("paymentIntentId", "pi_test_id");
        stripeResponse.put("amount", 10000L);
        stripeResponse.put("currency", "usd");
        stripeResponse.put("status", "requires_payment_method");
        when(paymentRepository.findById(1L)) .thenReturn(Optional.of(payment));
        when(stripePaymentService.createPaymentIntentWithMetadata(anyDouble(), anyString(), anyString(), any())) .thenReturn(stripeResponse);
        when(paymentTransactionRepository.save(any(PaymentTransaction.
        class))) .thenAnswer(invocation -> invocation.getArgument(0));
        java.util.Map<String, Object> result = onlinePaymentService.createPaymentIntentForPayment(1L);
        assertNotNull(result);
        assertEquals("pi_test_secret", result.get("clientSecret"));
        assertEquals("pi_test_id", result.get("paymentIntentId"));
        verify(paymentRepository, times(1)) .findById(1L);
        verify(stripePaymentService, times(1)) .createPaymentIntentWithMetadata(anyDouble(), anyString(), anyString(), any());
        verify(paymentTransactionRepository, times(1)) .save(any(PaymentTransaction.
        class));
    }
    @Test void createPaymentIntentForPayment_WhenPaymentNotFound_ShouldThrowException() {
        when(paymentRepository.findById(999L)) .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.
        class,() -> {
            onlinePaymentService.createPaymentIntentForPayment(999L);
        });
        verify(paymentRepository, times(1)) .findById(999L);
        verify(stripePaymentService, never()) .createPaymentIntentWithMetadata(anyDouble(), anyString(), anyString(), any());
    }
    @Test void processPaymentConfirmation_WhenSucceeded_ShouldUpdatePaymentStatus() throws StripeException {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(1L);
        transaction.setStripePaymentIntentId("pi_test_id");
        transaction.setPayment(payment);
        transaction.setAmount(100.0);
        transaction.setCurrency("USD");
        transaction.setStatus("PENDING");
        PaymentIntent paymentIntent = mock(PaymentIntent.
        class);
        when(paymentIntent.getStatus()) .thenReturn("succeeded");
        when(stripePaymentService.getPaymentIntent("pi_test_id")) .thenReturn(paymentIntent);
        when(paymentTransactionRepository.findByStripePaymentIntentId("pi_test_id")) .thenReturn(Optional.of(transaction));
        when(paymentTransactionRepository.save(any(PaymentTransaction.
        class))) .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.
        class))) .thenAnswer(invocation -> invocation.getArgument(0));
        PaymentTransaction result = onlinePaymentService.processPaymentConfirmation("pi_test_id");
        assertNotNull(result);
        assertEquals("SUCCEEDED", result.getStatus());
        assertNotNull(result.getCompletedAt());
        assertEquals("PAID", payment.getStatus());
        verify(stripePaymentService, times(1)) .getPaymentIntent("pi_test_id");
        verify(paymentTransactionRepository, times(1)) .save(any(PaymentTransaction.
        class));
        verify(paymentRepository, times(1)) .save(any(Payment.
        class));
    }
    @Test void processPaymentConfirmation_WhenTransactionNotFound_ShouldThrowException() throws StripeException {
        PaymentIntent paymentIntent = mock(PaymentIntent.
        class);
        when(paymentIntent.getStatus()) .thenReturn("succeeded");
        when(stripePaymentService.getPaymentIntent("pi_test_id")) .thenReturn(paymentIntent);
        when(paymentTransactionRepository.findByStripePaymentIntentId("pi_test_id")) .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.
        class,() -> {
            onlinePaymentService.processPaymentConfirmation("pi_test_id");
        });
        verify(stripePaymentService, times(1)) .getPaymentIntent("pi_test_id");
        verify(paymentTransactionRepository, never()) .save(any(PaymentTransaction.
        class));
    }
    @Test void cancelPayment_WhenValid_ShouldCancelPayment() throws StripeException {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(1L);
        transaction.setStripePaymentIntentId("pi_test_id");
        transaction.setStatus("PENDING");
        PaymentIntent paymentIntent = mock(PaymentIntent.
        class);
        when(paymentIntent.getStatus()) .thenReturn("canceled");
        when(stripePaymentService.cancelPaymentIntent("pi_test_id")) .thenReturn(paymentIntent);
        when(paymentTransactionRepository.findByStripePaymentIntentId("pi_test_id")) .thenReturn(Optional.of(transaction));
        when(paymentTransactionRepository.save(any(PaymentTransaction.
        class))) .thenAnswer(invocation -> invocation.getArgument(0));
        PaymentTransaction result = onlinePaymentService.cancelPayment("pi_test_id");
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        assertEquals("Payment canceled by user", result.getFailureReason());
        assertNotNull(result.getCompletedAt());
        verify(stripePaymentService, times(1)) .cancelPaymentIntent("pi_test_id");
        verify(paymentTransactionRepository, times(1)) .save(any(PaymentTransaction.
        class));
    }
    @Test void getTransactionByPaymentIntentId_WhenExists_ShouldReturnTransaction() {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(1L);
        transaction.setStripePaymentIntentId("pi_test_id");
        when(paymentTransactionRepository.findByStripePaymentIntentId("pi_test_id")) .thenReturn(Optional.of(transaction));
        Optional<PaymentTransaction> result = onlinePaymentService.getTransactionByPaymentIntentId("pi_test_id");
        assertTrue(result.isPresent());
        assertEquals(transaction, result.get());
        verify(paymentTransactionRepository, times(1)) .findByStripePaymentIntentId("pi_test_id");
    }
    @Test void getTransactionsByPaymentId_ShouldReturnTransactions() {
        PaymentTransaction transaction1 = new PaymentTransaction();
        transaction1.setId(1L);
        PaymentTransaction transaction2 = new PaymentTransaction();
        transaction2.setId(2L);
        when(paymentTransactionRepository.findByPaymentId(1L)) .thenReturn(java.util.Arrays.asList(transaction1, transaction2));
        java.util.List<PaymentTransaction> result = onlinePaymentService.getTransactionsByPaymentId(1L);
        assertEquals(2, result.size());
        assertTrue(result.contains(transaction1));
        assertTrue(result.contains(transaction2));
        verify(paymentTransactionRepository, times(1)) .findByPaymentId(1L);
    }
    @Test void checkPaymentStatus_ShouldReturnPaymentStatus() throws StripeException {
        PaymentIntent paymentIntent = mock(PaymentIntent.
        class);
        when(paymentIntent.getStatus()) .thenReturn("succeeded");
        when(paymentIntent.getAmount()) .thenReturn(10000L);
        when(paymentIntent.getCurrency()) .thenReturn("usd");
        when(paymentIntent.getCreated()) .thenReturn(1234567890L);
        when(paymentIntent.getMetadata()) .thenReturn(new java.util.HashMap<>());
        when(stripePaymentService.getPaymentIntent("pi_test_id")) .thenReturn(paymentIntent);
        java.util.Map<String, Object> result = onlinePaymentService.checkPaymentStatus("pi_test_id");
        assertNotNull(result);
        assertEquals("succeeded", result.get("status"));
        assertEquals(10000L, result.get("amount"));
        assertEquals("usd", result.get("currency"));
        verify(stripePaymentService, times(1)) .getPaymentIntent("pi_test_id");
    }
}
