package com.deskflow.ticketservice.model;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tickets")
@Getter
@NoArgsConstructor
public class Ticket {

  @Id private String id;
  private String title;
  private String description;
  @Setter private Status status;
  private Priority priority;
  private Category category;
  private String reporterId;
  @Setter private String assigneeId;
  private List<String> attachmentUrls;
  private Instant slaDeadline;
  @Setter private boolean slaBreached;
  @Setter private String resolutionNote;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  public Ticket(
      String title,
      String description,
      Priority priority,
      Category category,
      String reporterId,
      Instant slaDeadline) {
    this.title = title;
    this.description = description;
    this.priority = priority;
    this.category = category;
    this.reporterId = reporterId;
    this.slaDeadline = slaDeadline;
  }
}
