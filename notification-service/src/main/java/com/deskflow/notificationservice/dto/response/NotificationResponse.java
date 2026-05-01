package com.deskflow.notificationservice.dto.response;

import com.deskflow.notificationservice.model.KafkaEventType;
import com.deskflow.notificationservice.model.Type;
import java.time.Instant;

public record NotificationResponse(
    String id,
    String recipientId,
    Type type,
    KafkaEventType triggerEvent,
    String title,
    String body,
    String ticketId,
    boolean isRead,
    Instant sentAt) {}
