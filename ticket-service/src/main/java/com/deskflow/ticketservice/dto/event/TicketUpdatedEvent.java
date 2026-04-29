package com.deskflow.ticketservice.dto.event;

import com.deskflow.ticketservice.model.Priority;
import com.deskflow.ticketservice.model.Status;

public record TicketUpdatedEvent(
    String ticketId,
    Status previousStatus,
    Status newStatus,
    Priority priority,
    String reporterId,
    String assigneeId) {}
