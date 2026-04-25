package com.example.authservice.dto.request;

import com.example.authservice.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email String email, @NotBlank @Size(min = 8) String password, Role role) {}
