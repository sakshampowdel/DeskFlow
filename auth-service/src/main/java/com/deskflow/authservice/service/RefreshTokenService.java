package com.deskflow.authservice.service;

import com.deskflow.authservice.model.RefreshToken;
import com.deskflow.authservice.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  @Value("${jwt.refresh-expiration}")
  private long refreshExpiration;

  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshToken createRefreshToken(String userId) {
    RefreshToken token =
        new RefreshToken(
            UUID.randomUUID().toString(), userId, Instant.now().plusSeconds(refreshExpiration));
    return refreshTokenRepository.save(token);
  }

  public RefreshToken validate(String token) {
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new RuntimeException("Refresh token not found"));

    if (refreshToken.isRevoked()) {
      throw new RuntimeException("Refresh token revoked");
    }

    if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
      throw new RuntimeException("Refresh token expired");
    }

    return refreshToken;
  }

  public void revoke(String token) {
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new RuntimeException("Refresh token not found"));
    refreshToken.setRevoked(true);
    refreshTokenRepository.save(refreshToken);
  }
}
