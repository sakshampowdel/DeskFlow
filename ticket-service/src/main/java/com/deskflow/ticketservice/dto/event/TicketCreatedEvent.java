package com.deskflow.ticketservice.dto.event;

import com.deskflow.ticketservice.model.Category;
import com.deskflow.ticketservice.model.Priority;
import java.time.Instant;

public record TicketCreatedEvent(
    String ticketId,
    String title,
    Priority priority,
    Category category,
    String reporterId,
    Instant slaDeadline) {}
