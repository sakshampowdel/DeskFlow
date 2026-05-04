package com.deskflow.authservice.dto.event;

import com.deskflow.authservice.model.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserRegisteredEvent(
    @JsonProperty("userId") String userId,
    @JsonProperty("email") String email,
    @JsonProperty("role") Role role) {}
