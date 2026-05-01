package com.deskflow.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.deskflow.notificationservice.dto.request.PagedNotificationRequest;
import com.deskflow.notificationservice.dto.response.NotificationResponse;
import com.deskflow.notificationservice.dto.response.UnreadCountResponse;
import com.deskflow.notificationservice.exception.ForbiddenException;
import com.deskflow.notificationservice.model.KafkaEventType;
import com.deskflow.notificationservice.model.NotificationLog;
import com.deskflow.notificationservice.model.Type;
import com.deskflow.notificationservice.repository.NotificationLogRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationLogServiceTest {

  @Mock private NotificationLogRepository notificationLogRepository;
  @Mock private SimpMessagingTemplate messagingTemplate;

  @InjectMocks private NotificationLogService notificationLogService;

  @Test
  @DisplayName("getMyNotifications → Should filter by isRead when provided")
  void getMyNotifications_WithReadStatus_FiltersCorrectly() {
    String userId = "user-123";
    PagedNotificationRequest request = new PagedNotificationRequest(false, 0, 10);
    Page<NotificationLog> page = new PageImpl<>(List.of());

    when(notificationLogRepository.findByRecipientIdAndIsRead(
            eq(userId), eq(false), any(PageRequest.class)))
        .thenReturn(page);

    notificationLogService.getMyNotifications(userId, request);

    verify(notificationLogRepository)
        .findByRecipientIdAndIsRead(eq(userId), eq(false), any(PageRequest.class));
    verify(notificationLogRepository, never()).findByRecipientId(anyString(), any());
  }

  @Test
  @DisplayName("markAsRead → Should update status when user is the owner")
  void markAsRead_ValidOwner_Success() {
    String userId = "user-123";
    String notificationId = "note-001";
    NotificationLog log = new NotificationLog();
    log.setRecipientId(userId);
    log.setRead(false);
    ReflectionTestUtils.setField(log, "id", notificationId);

    when(notificationLogRepository.findById(notificationId)).thenReturn(Optional.of(log));

    notificationLogService.markAsRead(userId, notificationId);

    assertThat(log.isRead()).isTrue();
    verify(notificationLogRepository).save(log);
  }

  @Test
  @DisplayName("markAsRead → Should throw Forbidden when user is not the owner")
  void markAsRead_WrongUser_ThrowsForbidden() {
    String ownerId = "user-123";
    String hackerId = "hacker-456";
    String notificationId = "note-001";
    NotificationLog log = new NotificationLog();
    log.setRecipientId(ownerId);

    when(notificationLogRepository.findById(notificationId)).thenReturn(Optional.of(log));

    assertThrows(
        ForbiddenException.class,
        () -> notificationLogService.markAsRead(hackerId, notificationId));

    verify(notificationLogRepository, never()).save(any());
  }

  @Test
  @DisplayName("markAllAsRead → Should set all unread logs for user to read")
  void markAllAsRead_UpdatesAllUnread() {
    String userId = "user-123";
    NotificationLog n1 = new NotificationLog();
    n1.setRead(false);
    NotificationLog n2 = new NotificationLog();
    n2.setRead(false);

    Page<NotificationLog> unreadPage = new PageImpl<>(List.of(n1, n2));

    // Service calls findByRecipientIdAndIsRead twice in the current implementation
    when(notificationLogRepository.findByRecipientIdAndIsRead(
            eq(userId), eq(false), any(PageRequest.class)))
        .thenReturn(unreadPage);

    notificationLogService.markAllAsRead(userId);

    assertThat(n1.isRead()).isTrue();
    assertThat(n2.isRead()).isTrue();
    verify(notificationLogRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("getUnreadCount → Should return count from repository")
  void getUnreadCount_ReturnsValue() {
    String userId = "user-123";
    when(notificationLogRepository.countByRecipientIdAndIsRead(userId, false)).thenReturn(7L);

    UnreadCountResponse response = notificationLogService.getUnreadCount(userId);

    assertThat(response.count()).isEqualTo(7L);
  }

  @Test
  @DisplayName("pushToUser → Should send notification via SimpMessagingTemplate")
  void pushToUser_SendsWebSocketMessage() {
    String userId = "user-123";
    NotificationResponse response =
        new NotificationResponse(
            "id",
            userId,
            Type.WEBSOCKET_PUSH,
            KafkaEventType.TICKET_CREATED,
            "Title",
            "Body",
            "T-1",
            false,
            Instant.now());

    notificationLogService.pushToUser(userId, response);

    verify(messagingTemplate)
        .convertAndSendToUser(eq(userId), eq("/topic/notifications"), eq(response));
  }
}
