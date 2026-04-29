package com.deskflow.ticketservice.dto.response;

import com.deskflow.ticketservice.model.Category;
import com.deskflow.ticketservice.model.Priority;
import com.deskflow.ticketservice.model.Status;
import java.time.Instant;

public record TicketSummaryResponse(
    String id,
    String title,
    Status status,
    Priority priority,
    Category category,
    String reporterId,
    String assigneeId,
    Instant slaDeadline,
    boolean slaBreached,
    Instant createdAt
) {}
