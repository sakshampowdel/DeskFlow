package com.deskflow.userservice.dto.request;

import jakarta.annotation.Nullable;

public record UpdateProfileRequest(
    @Nullable String fullName, @Nullable String department, @Nullable String avatarUrl) {}
