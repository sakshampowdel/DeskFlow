package com.deskflow.ticketservice.dto.response;

import com.deskflow.ticketservice.model.Category;
import com.deskflow.ticketservice.model.Priority;
import com.deskflow.ticketservice.model.Status;
import java.time.Instant;
import java.util.List;

public record TicketResponse(
    String id,
    String title,
    String description,
    Status status,
    Priority priority,
    Category category,
    String reporterId,
    String assigneeId,
    List<String> attachmentUrls,
    Instant slaDeadline,
    boolean slaBreached,
    String resolutionNote,
    List<TicketCommentResponse> comments,
    Instant createdAt,
    Instant updatedAt) {}
