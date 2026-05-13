package com.footballacademy.services;

import com.footballacademy.DTO.PaymentDto;
import com.footballacademy.exception.PaymentException;
import com.footballacademy.exception.ResourceNotFoundException;
import com.footballacademy.exception.ValidationException;
import com.footballacademy.model.*;
import com.footballacademy.repository.PaymentRepository;
import com.footballacademy.repository.ParentRepository;
import com.footballacademy.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.
class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ParentRepository parentRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private PaymentService paymentService;
    private Payment testPayment;
    private Player testPlayer;
    private Parent testParent;
    @BeforeEach void setUp() {
        // Create test user
        User user = new User();
        user.setId(1L);
        user.setNom("Test User");
        user.setEmail("test@example.com");
        // Create test parent         testParent =
        new Parent();
        testParent.setId(1L);
        testParent.setUser(user);
        // Create test player         testPlayer =
        new Player();
        testPlayer.setId(1L);
        testPlayer.setUser(user);
        testPlayer.setParent(testParent);
        // Create test payment         testPayment =
        new Payment();
        testPayment.setId(1L);
        testPayment.setMontant(100.0);
        testPayment.setMois(LocalDate.now());
        testPayment.setPaid(false);
        testPayment.setPlayer(testPlayer);
        testPayment.setParent(testParent);
    }
    @Test void testCreatePayment_Success() {
        //
        Arrange when(playerRepository.findById(1L)) .thenReturn(Optional.of(testPlayer));
        when(parentRepository.findById(1L)) .thenReturn(Optional.of(testParent));
        when(paymentRepository.save(any(Payment.
        class))) .thenReturn(testPayment);
        // Act
        Payment result = paymentService.createPayment(1L, 100.0, LocalDate.now());
        //
        Assert assertNotNull(result);
        assertEquals(100.0, result.getMontant());
        verify(paymentRepository, times(1)) .save(any(Payment.
        class));
    }
    @Test void testCreatePayment_InvalidAmount() {
        // Act &
        Assert assertThrows(PaymentException.class,() -> paymentService.createPayment(1L, -100.0, LocalDate.now()));
        verify(paymentRepository, never()) .save(any(Payment.
        class));
    }
    @Test void testCreatePayment_PlayerNotFound() {
        //
        Arrange when(playerRepository.findById(1L)) .thenReturn(Optional.empty());
        // Act &
        Assert assertThrows(ResourceNotFoundException.class,() -> paymentService.createPayment(1L, 100.0, LocalDate.now()));
    }
    @Test void testUpdatePaymentStatus_Success() {
        //
        Arrange when(paymentRepository.findById(1L)) .thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.
        class))) .thenReturn(testPayment);
        // Act
        Payment result = paymentService.updatePaymentStatus(1L, true);
        //
        Assert assertNotNull(result);
        assertTrue(result.isPaid());
        assertEquals("PAID", result.getStatus());
        verify(paymentRepository, times(1)) .save(any(Payment.
        class));
    }
    @Test void testUpdatePaymentStatus_PaymentNotFound() {
        //
        Arrange when(paymentRepository.findById(1L)) .thenReturn(Optional.empty());
        // Act &
        Assert assertThrows(PaymentException.class,() -> paymentService.updatePaymentStatus(1L, true));
    }
    @Test void testGetUnpaidPaymentsForCurrentMonth_Success() {
        //
        Arrange when(paymentRepository.findUnpaidByMonth(anyInt(), anyInt())) .thenReturn(Arrays.asList(testPayment));
        // Act
        List<PaymentDto> result = paymentService.getUnpaidPaymentsForCurrentMonth();
        //
        Assert assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(paymentRepository, times(1)) .findUnpaidByMonth(anyInt(), anyInt());
    }
    @Test void testGetPaymentsForMonth_Success() {
        //
        Arrange when(paymentRepository.findByMois(any(LocalDate.
        class))) .thenReturn(Arrays.asList(testPayment));
        // Act
        List<PaymentDto> result = paymentService.getPaymentsForMonth(2024, 5);
        //
        Assert assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(paymentRepository, times(1)) .findByMois(any(LocalDate.
        class));
    }
    @Test void testGetPaymentsForMonth_InvalidYear() {
        // Act &
        Assert assertThrows(ValidationException.class,() -> paymentService.getPaymentsForMonth(1999, 5));
    }
    @Test void testGetPaymentsForMonth_InvalidMonth() {
        // Act &
        Assert assertThrows(ValidationException.class,() -> paymentService.getPaymentsForMonth(2024, 13));
    }
    @Test void testDeletePayment_Success() {
        //
        Arrange when(paymentRepository.existsById(1L)) .thenReturn(true);
        doNothing() .when(paymentRepository) .deleteById(1L);
        // Act
        paymentService.deletePayment(1L);
        //
        Assert verify(paymentRepository, times(1)) .deleteById(1L);
    }
    @Test void testDeletePayment_NotFound() {
        //
        Arrange when(paymentRepository.existsById(1L)) .thenReturn(false);
        // Act &
        Assert assertThrows(PaymentException.class,() -> paymentService.deletePayment(1L));
    }
}
