package com.deskflow.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.deskflow.authservice.model.AuthUser;
import com.deskflow.authservice.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    // Use a 256-bit key for HMAC-SHA
    String secret = "vO7S98u6pX5aY8M3dE2fG1hJ4kL7mN0pQ3rS6tU9vW2xZ5y8";
    ReflectionTestUtils.setField(jwtService, "secret", secret);
    ReflectionTestUtils.setField(jwtService, "expiration", 3600L);
  }

  @Test
  void generateAccessToken_createsValidToken() {
    AuthUser user = new AuthUser("test@test.com", "hash", Role.SUBMITTER);
    ReflectionTestUtils.setField(user, "id", "user-123");

    String token = jwtService.generateAccessToken(user);

    assertThat(token).isNotNull();
    assertThat(token.split("\\.")).hasSize(3); // Header.Payload.Signature
  }
}
