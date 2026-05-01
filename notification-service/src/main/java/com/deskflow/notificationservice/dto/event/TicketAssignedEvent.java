package com.deskflow.notificationservice.dto.event;

public record TicketAssignedEvent(
    String ticketId, String title, String assigneeId, String assignedById) {}
