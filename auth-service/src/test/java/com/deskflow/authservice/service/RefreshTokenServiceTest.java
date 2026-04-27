package com.deskflow.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.deskflow.authservice.exception.InvalidTokenException;
import com.deskflow.authservice.exception.TokenExpiredException;
import com.deskflow.authservice.model.RefreshToken;
import com.deskflow.authservice.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock private RefreshTokenRepository refreshTokenRepository;
  @InjectMocks private RefreshTokenService refreshTokenService;

  @Test
  @DisplayName("Validate should throw exception if token is revoked")
  void validate_tokenRevoked_throwsException() {
    RefreshToken token = new RefreshToken("token", "user", Instant.now().plusSeconds(100));
    token.setRevoked(true);

    when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

    assertThrows(InvalidTokenException.class, () -> refreshTokenService.validate("token"));
  }

  @Test
  @DisplayName("Validate should throw exception if token is expired")
  void validate_tokenExpired_throwsException() {
    RefreshToken token = new RefreshToken("token", "user", Instant.now().minusSeconds(100));

    when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

    assertThrows(TokenExpiredException.class, () -> refreshTokenService.validate("token"));
  }

  @Test
  @DisplayName("Revoke should set revoked to true and save")
  void revoke_updatesTokenStatus() {
    RefreshToken token = new RefreshToken("token", "user", Instant.now().plusSeconds(100));
    when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(token));

    refreshTokenService.revoke("token");

    assertThat(token.isRevoked()).isTrue();
    verify(refreshTokenRepository).save(token);
  }
}
