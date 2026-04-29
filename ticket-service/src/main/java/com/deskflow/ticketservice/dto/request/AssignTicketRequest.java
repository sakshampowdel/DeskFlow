package com.deskflow.ticketservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AssignTicketRequest(@NotBlank String assigneeId) {}
