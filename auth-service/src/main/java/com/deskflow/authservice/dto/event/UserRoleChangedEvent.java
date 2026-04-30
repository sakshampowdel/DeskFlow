package com.deskflow.authservice.dto.event;

import com.deskflow.authservice.model.Role;

public record UserRoleChangedEvent(String userId, Role previousRole, Role newRole) {}
