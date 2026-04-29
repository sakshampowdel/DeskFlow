package com.deskflow.ticketservice.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TicketComment {
  private String id;
  private String authorId;
  private String body;
  private boolean isInternal;

  private Instant createdAt;

  public TicketComment(String authorId, String body, boolean isInternal) {
    this.id = UUID.randomUUID().toString();
    this.authorId = authorId;
    this.body = body;
    this.isInternal = isInternal;
    this.createdAt = Instant.now();
  }
}
