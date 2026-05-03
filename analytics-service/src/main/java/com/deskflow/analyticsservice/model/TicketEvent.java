package com.deskflow.analyticsservice.model;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ticket_events")
@Getter
@NoArgsConstructor
public class TicketEvent {

  @Id private String id;
  private String ticketId;
  private KafkaEventType eventType;
  private Status status;
  private Priority priority;
  private Category category;
  private String reporterId;
  private String assigneeId;
  private Instant occurredAt;

  public TicketEvent(
      String ticketId,
      KafkaEventType eventType,
      Status status,
      Priority priority,
      Category category,
      String reporterId,
      String assigneeId,
      Instant occurredAt) {
    this.ticketId = ticketId;
    this.eventType = eventType;
    this.status = status;
    this.priority = priority;
    this.category = category;
    this.reporterId = reporterId;
    this.assigneeId = assigneeId;
    this.occurredAt = occurredAt;
  }
}
