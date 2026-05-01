package com.deskflow.notificationservice.controller;

import com.deskflow.notificationservice.dto.request.PagedNotificationRequest;
import com.deskflow.notificationservice.dto.response.PagedNotificationResponse;
import com.deskflow.notificationservice.dto.response.UnreadCountResponse;
import com.deskflow.notificationservice.service.NotificationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationLogController {

  private final NotificationLogService notificationLogService;

  @GetMapping
  public ResponseEntity<PagedNotificationResponse> getMyNotifications(
      @RequestHeader("X-User-Id") String userId,
      @ModelAttribute @Valid PagedNotificationRequest request) {
    return ResponseEntity.ok(notificationLogService.getMyNotifications(userId, request));
  }

  @PatchMapping("/{notificationId}/read")
  public ResponseEntity<Void> markAsRead(
      @RequestHeader("X-User-Id") String userId, @PathVariable String notificationId) {
    notificationLogService.markAsRead(userId, notificationId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/read-all")
  public ResponseEntity<Void> markAllAsRead(@RequestHeader("X-User-Id") String userId) {
    notificationLogService.markAllAsRead(userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/unread-count")
  public ResponseEntity<UnreadCountResponse> getUnreadCount(
      @RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok(notificationLogService.getUnreadCount(userId));
  }
}
