package com.deskflow.ticketservice.dto.response;

import java.time.Instant;

public record TicketCommentResponse(
    String id, String authorId, String body, boolean isInternal, Instant createdAt) {}
