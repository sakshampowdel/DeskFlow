package com.deskflow.userservice.dto.response;

import com.deskflow.userservice.model.Role;
import java.time.Instant;

public record UserProfileResponse(
    String id,
    String email,
    String fullName,
    Role role,
    String department,
    String avatarUrl,
    boolean isActive,
    Instant createdAt,
    Instant updatedAt) {}
