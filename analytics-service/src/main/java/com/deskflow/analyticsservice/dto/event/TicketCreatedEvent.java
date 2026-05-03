package com.deskflow.analyticsservice.dto.event;

import com.deskflow.analyticsservice.model.Category;
import com.deskflow.analyticsservice.model.Priority;
import java.time.Instant;

public record TicketCreatedEvent(
    String ticketId,
    String title,
    Priority priority,
    Category category,
    String reporterId,
    Instant slaDeadline) {}
