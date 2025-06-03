package com.hdfc.bank.notification.service;

import com.hdfc.bank.notification.dto.NotificationRequest;
import com.hdfc.bank.notification.dto.NotificationResponse;
import com.hdfc.bank.notification.model.Notification;
import com.hdfc.bank.notification.repository.NotificationRepository;
import com.hdfc.bank.notification.util.NotificationMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private SmsService smsService;

    @Mock
    private EmailService emailService;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;
    private NotificationResponse notificationResponse;
    private NotificationRequest notificationRequest;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setNotificationId("NOTIF001");
        notification.setUserId("USER001");
        notification.setType(Notification.NotificationType.TRANSACTION_ALERT);
        notification.setChannel(Notification.NotificationChannel.SMS);
        notification.setTitle("Transaction Alert");
        notification.setMessage("Your account has been debited with Rs. 5000");
        notification.setStatus(Notification.NotificationStatus.SENT);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSentAt(LocalDateTime.now());

        notificationResponse = new NotificationResponse();
        notificationResponse.setNotificationId("NOTIF001");
        notificationResponse.setUserId("USER001");
        notificationResponse.setType(Notification.NotificationType.TRANSACTION_ALERT);
        notificationResponse.setChannel(Notification.NotificationChannel.SMS);
        notificationResponse.setTitle("Transaction Alert");
        notificationResponse.setMessage("Your account has been debited with Rs. 5000");
        notificationResponse.setStatus(Notification.NotificationStatus.SENT);

        notificationRequest = new NotificationRequest();
        notificationRequest.setUserId("USER001");
        notificationRequest.setType(Notification.NotificationType.TRANSACTION_ALERT);
        notificationRequest.setChannel(Notification.NotificationChannel.SMS);
        notificationRequest.setTitle("Transaction Alert");
        notificationRequest.setMessage("Your account has been debited with Rs. 5000");
        notificationRequest.setRecipient("9876543210");
    }

    @Test
    void sendNotification_SMS_Success() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(smsService.sendSms(anyString(), anyString())).thenReturn(true);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        // When
        NotificationResponse result = notificationService.sendNotification(notificationRequest);

        // Then
        assertNotNull(result);
        assertEquals("NOTIF001", result.getNotificationId());
        assertEquals(Notification.NotificationStatus.SENT, result.getStatus());
        verify(smsService).sendSms("9876543210", "Your account has been debited with Rs. 5000");
        verify(notificationRepository, times(2)).save(any(Notification.class)); // Initial save + status update
        verify(notificationMapper).toResponse(notification);
    }

    @Test
    void sendNotification_Email_Success() {
        // Given
        notificationRequest.setChannel(Notification.NotificationChannel.EMAIL);
        notificationRequest.setRecipient("john.doe@example.com");
        notification.setChannel(Notification.NotificationChannel.EMAIL);
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        // When
        NotificationResponse result = notificationService.sendNotification(notificationRequest);

        // Then
        assertNotNull(result);
        verify(emailService).sendEmail("john.doe@example.com", "Transaction Alert", "Your account has been debited with Rs. 5000");
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendNotification_Push_Success() {
        // Given
        notificationRequest.setChannel(Notification.NotificationChannel.PUSH);
        notificationRequest.setRecipient("USER001");
        notification.setChannel(Notification.NotificationChannel.PUSH);
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(pushNotificationService.sendPushNotification(anyString(), anyString(), anyString())).thenReturn(true);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        // When
        NotificationResponse result = notificationService.sendNotification(notificationRequest);

        // Then
        assertNotNull(result);
        verify(pushNotificationService).sendPushNotification("USER001", "Transaction Alert", "Your account has been debited with Rs. 5000");
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendNotification_SMS_Failure() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(smsService.sendSms(anyString(), anyString())).thenReturn(false);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        // When
        NotificationResponse result = notificationService.sendNotification(notificationRequest);

        // Then
        assertNotNull(result);
        verify(smsService).sendSms("9876543210", "Your account has been debited with Rs. 5000");
        verify(notificationRepository, times(2)).save(any(Notification.class));
        // Verify status is updated to FAILED
        verify(notificationRepository).save(argThat(n -> n.getStatus() == Notification.NotificationStatus.FAILED));
    }

    @Test
    void getNotificationById_Success() {
        // Given
        when(notificationRepository.findByNotificationId("NOTIF001")).thenReturn(Optional.of(notification));
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        // When
        NotificationResponse result = notificationService.getNotificationById("NOTIF001");

        // Then
        assertNotNull(result);
        assertEquals("NOTIF001", result.getNotificationId());
        verify(notificationRepository).findByNotificationId("NOTIF001");
        verify(notificationMapper).toResponse(notification);
    }

    @Test
    void getNotificationsByUserId_Success() {
        // Given
        List<Notification> notifications = Arrays.asList(notification);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("USER001")).thenReturn(notifications);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        // When
        List<NotificationResponse> result = notificationService.getNotificationsByUserId("USER001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("NOTIF001", result.get(0).getNotificationId());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc("USER001");
        verify(notificationMapper).toResponse(notification);
    }

    @Test
    void markAsRead_Success() {
        // Given
        notification.setStatus(Notification.NotificationStatus.DELIVERED);
        when(notificationRepository.findByNotificationId("NOTIF001")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.markAsRead("NOTIF001");

        // Then
        verify(notificationRepository).findByNotificationId("NOTIF001");
        verify(notificationRepository).save(argThat(n -> n.getStatus() == Notification.NotificationStatus.READ));
    }

    @Test
    void getUnreadNotifications_Success() {
        // Given
        List<Notification> unreadNotifications = Arrays.asList(notification);
        when(notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc("USER001", Notification.NotificationStatus.DELIVERED))
                .thenReturn(unreadNotifications);
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        // When
        List<NotificationResponse> result = notificationService.getUnreadNotifications("USER001");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificationRepository).findByUserIdAndStatusOrderByCreatedAtDesc("USER001", Notification.NotificationStatus.DELIVERED);
    }

    @Test
    void sendBulkNotification_Success() {
        // Given
        List<String> userIds = Arrays.asList("USER001", "USER002", "USER003");
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(smsService.sendSms(anyString(), anyString())).thenReturn(true);

        // When
        notificationService.sendBulkNotification(userIds, "Bulk Alert", "System maintenance scheduled", 
                Notification.NotificationType.SYSTEM_ALERT, Notification.NotificationChannel.SMS);

        // Then
        verify(notificationRepository, times(6)).save(any(Notification.class)); // 3 initial saves + 3 status updates
        verify(smsService, times(3)).sendSms(anyString(), anyString());
    }

    @Test
    void scheduleNotification_Success() {
        // Given
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(1);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.scheduleNotification(notificationRequest, scheduledTime);

        // Then
        verify(notificationRepository).save(argThat(n -> 
                n.getStatus() == Notification.NotificationStatus.SCHEDULED &&
                n.getScheduledAt() != null
        ));
    }

    @Test
    void processScheduledNotifications_Success() {
        // Given
        List<Notification> scheduledNotifications = Arrays.asList(notification);
        notification.setStatus(Notification.NotificationStatus.SCHEDULED);
        notification.setChannel(Notification.NotificationChannel.SMS);
        notification.setRecipient("9876543210");
        
        when(notificationRepository.findByStatusAndScheduledAtBefore(
                eq(Notification.NotificationStatus.SCHEDULED), any(LocalDateTime.class)))
                .thenReturn(scheduledNotifications);
        when(smsService.sendSms(anyString(), anyString())).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.processScheduledNotifications();

        // Then
        verify(notificationRepository).findByStatusAndScheduledAtBefore(
                eq(Notification.NotificationStatus.SCHEDULED), any(LocalDateTime.class));
        verify(smsService).sendSms("9876543210", notification.getMessage());
        verify(notificationRepository).save(argThat(n -> n.getStatus() == Notification.NotificationStatus.SENT));
    }

    @Test
    void retryFailedNotifications_Success() {
        // Given
        List<Notification> failedNotifications = Arrays.asList(notification);
        notification.setStatus(Notification.NotificationStatus.FAILED);
        notification.setChannel(Notification.NotificationChannel.SMS);
        notification.setRecipient("9876543210");
        notification.setRetryCount(1);
        
        when(notificationRepository.findByStatusAndRetryCountLessThan(
                Notification.NotificationStatus.FAILED, 3)).thenReturn(failedNotifications);
        when(smsService.sendSms(anyString(), anyString())).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.retryFailedNotifications();

        // Then
        verify(notificationRepository).findByStatusAndRetryCountLessThan(Notification.NotificationStatus.FAILED, 3);
        verify(smsService).sendSms("9876543210", notification.getMessage());
        verify(notificationRepository).save(argThat(n -> 
                n.getStatus() == Notification.NotificationStatus.SENT &&
                n.getRetryCount() == 2
        ));
    }

    @Test
    void deleteOldNotifications_Success() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        when(notificationRepository.deleteByCreatedAtBefore(cutoffDate)).thenReturn(10);

        // When
        int deletedCount = notificationService.deleteOldNotifications(90);

        // Then
        assertEquals(10, deletedCount);
        verify(notificationRepository).deleteByCreatedAtBefore(any(LocalDateTime.class));
    }

    @Test
    void getNotificationStatistics_Success() {
        // Given
        when(notificationRepository.countByUserIdAndStatus("USER001", Notification.NotificationStatus.SENT))
                .thenReturn(5L);
        when(notificationRepository.countByUserIdAndStatus("USER001", Notification.NotificationStatus.FAILED))
                .thenReturn(2L);
        when(notificationRepository.countByUserIdAndStatus("USER001", Notification.NotificationStatus.DELIVERED))
                .thenReturn(3L);

        // When
        var stats = notificationService.getNotificationStatistics("USER001");

        // Then
        assertNotNull(stats);
        assertEquals(5L, stats.get("sent"));
        assertEquals(2L, stats.get("failed"));
        assertEquals(3L, stats.get("unread"));
    }
}