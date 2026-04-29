package com.deskflow.ticketservice.dto.request;

import com.deskflow.ticketservice.model.Category;
import com.deskflow.ticketservice.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 5000) String description,
    @NotNull Priority priority,
    @NotNull Category category) {}
