package com.example.authservice.dto.reponse;

public record AuthTokenResponse(String accessToken, String refreshToken, int expiresIn) {}
