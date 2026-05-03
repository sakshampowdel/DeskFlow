package com.deskflow.analyticsservice.model;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ticket_metrics")
@Getter
@NoArgsConstructor
public class TicketMetric {

  @Id private String id;
  private String ticketId;
  private Priority priority;
  private Category category;
  private Instant createdAt;

  @Setter private String assigneeId;
  @Setter private Instant resolvedAt;
  @Setter private Long timeToResolveMs;
  @Setter private Boolean slaBreached;
  @Setter private String resolutionStatus;

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
