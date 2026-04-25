package com.example.authservice.dto.reponse;

import com.example.authservice.model.Role;

public record RegisterResponse(String id, String email, Role role) {}
