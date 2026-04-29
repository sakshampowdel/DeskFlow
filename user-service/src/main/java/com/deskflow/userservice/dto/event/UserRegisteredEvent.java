package com.deskflow.userservice.dto.event;

import com.deskflow.userservice.model.Role;

public record UserRegisteredEvent(String userId, String email, Role role) {}
