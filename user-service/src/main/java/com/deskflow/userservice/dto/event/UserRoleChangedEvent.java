package com.deskflow.userservice.dto.event;

import com.deskflow.userservice.model.Role;

public record UserRoleChangedEvent(String userId, Role previousRole, Role newRole) {}
