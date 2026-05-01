package com.deskflow.notificationservice.dto.event;

public record TicketUpdatedEvent(
    String ticketId,
    String previousStatus,
    String newStatus,
    String priority,
    String reporterId,
    String assigneeId) {}
