package com.footballacademy.services.notification;

import com.footballacademy.model.Activity;
import com.footballacademy.model.Notification;
import com.footballacademy.model.Payment;
import com.footballacademy.model.User;
import com.footballacademy.repository.ActivityRepository;
import com.footballacademy.repository.NotificationRepository;
import com.footballacademy.repository.PaymentRepository;
import com.footballacademy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.
class)
class AdvancedNotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AdvancedNotificationService notificationService;
    private User user;
    private Activity activity;
    private Payment payment;
    @BeforeEach void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setNom("Test User");
        activity = new Activity();
        activity.setId(1L);
        activity.setTitle("Training Session");
        activity.setDate(LocalDateTime.now() .plusDays(1));
        payment = new Payment();
        payment.setId(1L);
        payment.setAmount(100.0);
        payment.setCurrency("USD");
        payment.setStatus("PENDING");
        payment.setDueDate(LocalDateTime.now() .minusDays(10));
    }
    @Test void sendCustomNotification_WhenValid_ShouldReturnNotification() {
        when(userRepository.findById(1L)) .thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.
        class))) .thenAnswer(invocation -> invocation.getArgument(0));
        Notification result = notificationService.sendCustomNotification(1L, "Test Title", "Test Message", "INFO");
        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Message", result.getMessage());
        assertEquals("INFO", result.getType());
        verify(userRepository, times(1)) .findById(1L);
        verify(notificationRepository, times(1)) .save(any(Notification.
        class));
    }
    @Test void sendCustomNotification_WhenUserNotFound_ShouldThrowException() {
        when(userRepository.findById(999L)) .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.
        class,() -> {
            notificationService.sendCustomNotification(999L, "Test Title", "Test Message", "INFO");
        });
        verify(userRepository, times(1)) .findById(999L);
        verify(notificationRepository, never()) .save(any(Notification.
        class));
    }
    @Test void sendBroadcastNotification_ShouldSendToAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        when(userRepository.findAll()) .thenReturn(Arrays.asList(user, user2));
        when(notificationRepository.save(any(Notification.
        class))) .thenAnswer(invocation -> invocation.getArgument(0));
        List<Notification> result = notificationService.sendBroadcastNotification("Broadcast Title", "Broadcast Message", "INFO");
        assertEquals(2, result.size());
        verify(userRepository, times(1)) .findAll();
        verify(notificationRepository, times(2)) .save(any(Notification.
        class));
    }
    @Test void sendNotificationByRole_ShouldSendToUsersWithRole() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole("ADMIN");
        when(userRepository.findByRole("ADMIN")) .thenReturn(Arrays.asList(user, adminUser));
        when(notificationRepository.save(any(Notification.
        class))) .thenAnswer(invocation -> invocation.getArgument(0));
        List<Notification> result = notificationService.sendNotificationByRole("ADMIN", "Admin Title", "Admin Message", "INFO");
        assertEquals(2, result.size());
        verify(userRepository, times(1)) .findByRole("ADMIN");
        verify(notificationRepository, times(2)) .save(any(Notification.
        class));
    }
    @Test void markAsRead_WhenValid_ShouldMarkNotificationAsRead() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setRead(false);
        when(notificationRepository.findById(1L)) .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.
        class))) .thenAnswer(invocation -> invocation.getArgument(0));
        notificationService.markAsRead(1L);
        assertTrue(notification.getRead());
        verify(notificationRepository, times(1)) .findById(1L);
        verify(notificationRepository, times(1)) .save(notification);
    }
    @Test void markAsRead_WhenNotExists_ShouldThrowException() {
        when(notificationRepository.findById(999L)) .thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.
        class,() -> {
            notificationService.markAsRead(999L);
        });
        verify(notificationRepository, times(1)) .findById(999L);
        verify(notificationRepository, never()) .save(any(Notification.
        class));
    }
    @Test void markAllAsRead_ShouldMarkAllUnreadAsRead() {
        Notification notification1 = new Notification();
        notification1.setId(1L);
        notification1.setRead(false);
        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setRead(false);
        when(notificationRepository.findByUserIdAndReadFalse(1L)) .thenReturn(Arrays.asList(notification1, notification2));
        when(notificationRepository.saveAll(anyList())) .thenAnswer(invocation -> invocation.getArgument(0));
        notificationService.markAllAsRead(1L);
        assertTrue(notification1.getRead());
        assertTrue(notification2.getRead());
        verify(notificationRepository, times(1)) .findByUserIdAndReadFalse(1L);
        verify(notificationRepository, times(1)) .saveAll(anyList());
    }
    @Test void getUnreadNotifications_ShouldReturnUnreadNotifications() {
        Notification notification1 = new Notification();
        notification1.setId(1L);
        notification1.setRead(false);
        Notification notification2 = new Notification();
        notification2.setId(2L);
        notification2.setRead(false);
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(1L)) .thenReturn(Arrays.asList(notification1, notification2));
        List<Notification> result = notificationService.getUnreadNotifications(1L);
        assertEquals(2, result.size());
        assertTrue(result.contains(notification1));
        assertTrue(result.contains(notification2));
        verify(notificationRepository, times(1)) .findByUserIdAndReadFalseOrderByCreatedAtDesc(1L);
    }
    @Test void getAllNotifications_ShouldReturnAllNotifications() {
        Notification notification1 = new Notification();
        notification1.setId(1L);
        Notification notification2 = new Notification();
        notification2.setId(2L);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)) .thenReturn(Arrays.asList(notification1, notification2));
        List<Notification> result = notificationService.getAllNotifications(1L);
        assertEquals(2, result.size());
        assertTrue(result.contains(notification1));
        assertTrue(result.contains(notification2));
        verify(notificationRepository, times(1)) .findByUserIdOrderByCreatedAtDesc(1L);
    }
}
