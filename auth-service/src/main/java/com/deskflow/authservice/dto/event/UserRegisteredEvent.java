package com.deskflow.authservice.dto.event;

import com.deskflow.authservice.model.Role;

public record UserRegisteredEvent(String userId, String email, Role role) {}
