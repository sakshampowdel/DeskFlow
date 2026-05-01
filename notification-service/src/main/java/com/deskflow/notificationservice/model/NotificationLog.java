package com.deskflow.notificationservice.model;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notification_logs")
@Getter
@NoArgsConstructor
public class NotificationLog {

  @Id private String id;
  @Setter private String recipientId;
  @Setter private Type type;
  @Setter private KafkaEventType triggerEvent;
  @Setter private String title;
  @Setter private String body;
  @Setter private String ticketId;
  @Setter private boolean isRead;

  @CreatedDate private Instant sentAt;

  public NotificationLog(
      String recipientId,
      Type type,
      KafkaEventType triggerEvent,
      String title,
      String body,
      String ticketId) {
    this.recipientId = recipientId;
    this.type = type;
    this.triggerEvent = triggerEvent;
    this.title = title;
    this.body = body;
    this.ticketId = ticketId;
  }
}
