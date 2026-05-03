package com.deskflow.analyticsservice.dto.event;

import com.deskflow.analyticsservice.model.Priority;
import com.deskflow.analyticsservice.model.Status;

public record TicketUpdatedEvent(
    String ticketId,
    Status previousStatus,
    Status newStatus,
    Priority priority,
    String reporterId,
    String assigneeId) {}
