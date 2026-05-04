package com.deskflow.analyticsservice.model;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ticket_metrics")
@Getter
@Setter
@NoArgsConstructor
public class TicketMetric {

  @Id private String id;
  private String ticketId;
  private Priority priority;
  private Category category;
  private Instant createdAt;

  private String assigneeId;
  private Instant resolvedAt;
  private Long timeToResolveMs;
  private Boolean slaBreached;
  private String resolutionStatus;

  public TicketMetric(
      String ticketId, Priority priority, Category category, String assigneeId, Instant createdAt) {
    this.id = ticketId;
    this.ticketId = ticketId;
    this.priority = priority;
    this.category = category;
    this.assigneeId = assigneeId;
    this.createdAt = createdAt;
  }
}
