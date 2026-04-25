package com.example.authservice.dto.request;

import com.example.authservice.model.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull Role role) {}
