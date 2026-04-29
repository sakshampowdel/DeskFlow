package com.deskflow.ticketservice.dto.event;

import java.time.Instant;

public record TicketResolvedEvent(
    String ticketId,
    String title,
    String reporterId,
    String assigneeId,
    Instant resolvedAt,
    String resolutionNote,
    boolean slaBreached) {}
