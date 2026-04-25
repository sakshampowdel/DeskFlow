package com.deskflow.authservice.dto.reponse;

import com.deskflow.authservice.model.Role;

public record RegisterResponse(String id, String email, Role role) {}
