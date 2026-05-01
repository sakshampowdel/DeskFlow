package com.deskflow.notificationservice.dto.event;

import java.time.Instant;

public record TicketCreatedEvent(
    String ticketId,
    String title,
    String priority,
    String category,
    String reporterId,
    Instant slaDeadline) {}
