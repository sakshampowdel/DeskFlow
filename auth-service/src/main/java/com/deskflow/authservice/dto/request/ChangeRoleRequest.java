package com.deskflow.authservice.dto.request;

import com.deskflow.authservice.model.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull Role role) {}
