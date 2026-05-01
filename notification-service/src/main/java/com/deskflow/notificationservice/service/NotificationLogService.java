package com.deskflow.notificationservice.service;

import com.deskflow.notificationservice.dto.request.PagedNotificationRequest;
import com.deskflow.notificationservice.dto.response.NotificationResponse;
import com.deskflow.notificationservice.dto.response.PagedNotificationResponse;
import com.deskflow.notificationservice.dto.response.UnreadCountResponse;
import com.deskflow.notificationservice.exception.ForbiddenException;
import com.deskflow.notificationservice.exception.NotificationNotFoundException;
import com.deskflow.notificationservice.model.NotificationLog;
import com.deskflow.notificationservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationLogService {

  private final NotificationLogRepository notificationLogRepository;
  private final SimpMessagingTemplate messagingTemplate;

  private NotificationLog findById(String notificationId) {
    return notificationLogRepository
        .findById(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));
  }

  private NotificationResponse mapToResponse(NotificationLog log) {
    return new NotificationResponse(
        log.getId(),
        log.getRecipientId(),
        log.getType(),
        log.getTriggerEvent(),
        log.getTitle(),
        log.getBody(),
        log.getTicketId(),
        log.isRead(),
        log.getSentAt());
  }

  public PagedNotificationResponse getMyNotifications(
      String userId, PagedNotificationRequest request) {
    Page<NotificationLog> page;
    PageRequest pageRequest = PageRequest.of(request.page(), request.size());

    if (request.isRead() != null) {
      page =
          notificationLogRepository.findByRecipientIdAndIsRead(
              userId, request.isRead(), pageRequest);
    } else {
      page = notificationLogRepository.findByRecipientId(userId, pageRequest);
    }

    return new PagedNotificationResponse(
        page.getContent().stream().map(this::mapToResponse).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isLast());
  }

  public void markAsRead(String userId, String notificationId) {
    NotificationLog log = findById(notificationId);
    if (!log.getRecipientId().equals(userId)) {
      throw new ForbiddenException("Access denied");
    }
    log.setRead(true);
    notificationLogRepository.save(log);
  }

  public void markAllAsRead(String userId) {
    notificationLogRepository
        .findByRecipientIdAndIsRead(userId, false, PageRequest.of(0, Integer.MAX_VALUE))
        .getContent()
        .forEach(log -> log.setRead(true));
    notificationLogRepository.saveAll(
        notificationLogRepository
            .findByRecipientIdAndIsRead(userId, false, PageRequest.of(0, Integer.MAX_VALUE))
            .getContent());
  }

  public UnreadCountResponse getUnreadCount(String userId) {
    return new UnreadCountResponse(
        notificationLogRepository.countByRecipientIdAndIsRead(userId, false));
  }

  public void pushToUser(String userId, NotificationResponse notification) {
    messagingTemplate.convertAndSendToUser(userId, "/topic/notifications", notification);
  }
}
