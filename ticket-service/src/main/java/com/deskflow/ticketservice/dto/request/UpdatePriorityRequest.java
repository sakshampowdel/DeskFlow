package com.deskflow.ticketservice.dto.request;

import com.deskflow.ticketservice.model.Priority;
import jakarta.validation.constraints.NotNull;

public record UpdatePriorityRequest(@NotNull Priority priority) {}
