package com.deskflow.ticketservice.dto.request;

import com.deskflow.ticketservice.model.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull Status status, String resolutionNote) {}
