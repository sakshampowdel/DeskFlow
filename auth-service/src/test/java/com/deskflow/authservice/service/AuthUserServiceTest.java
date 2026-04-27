package com.deskflow.authservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.deskflow.authservice.dto.reponse.AuthTokenResponse;
import com.deskflow.authservice.dto.request.LoginRequest;
import com.deskflow.authservice.dto.request.RegisterRequest;
import com.deskflow.authservice.exception.EmailAlreadyExistsException;
import com.deskflow.authservice.exception.InvalidCredentialsException;
import com.deskflow.authservice.model.AuthUser;
import com.deskflow.authservice.model.RefreshToken;
import com.deskflow.authservice.model.Role;
import com.deskflow.authservice.repository.AuthUserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthUserServiceTest {

  @Mock private AuthUserRepository authUserRepository;
  @Mock private BCryptPasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private AuthUserService authUserService;

  @Test
  @DisplayName("Register user should throw exception if email exists")
  void registerUser_emailExists_throwsException() {
    RegisterRequest request = new RegisterRequest("test@test.com", "password");
    when(authUserRepository.existsByEmail(request.email())).thenReturn(true);

    assertThrows(EmailAlreadyExistsException.class, () -> authUserService.registerUser(request));
  }

  @Test
  @DisplayName("Login user should return tokens on valid credentials")
  void loginUser_validCredentials_returnsTokens() {
    // Arrange
    LoginRequest request = new LoginRequest("test@test.com", "password");
    AuthUser user = new AuthUser("test@test.com", "encodedPass", Role.SUBMITTER);
    RefreshToken token = new RefreshToken("ref-123", "id-123", Instant.now().plusSeconds(100));

    when(authUserRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
    when(refreshTokenService.createRefreshToken(any())).thenReturn(token);
    when(jwtService.generateAccessToken(user)).thenReturn("access-token");
    when(jwtService.getExpiration()).thenReturn(3600L);

    // Act
    AuthTokenResponse response = authUserService.loginUser(request);

    // Assert
    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("ref-123");
    verify(refreshTokenService).createRefreshToken(any());
  }

  @Test
  @DisplayName("Login user should throw exception on wrong password")
  void loginUser_invalidPassword_throwsException() {
    LoginRequest request = new LoginRequest("test@test.com", "wrong");
    AuthUser user = new AuthUser("test@test.com", "encodedPass", Role.SUBMITTER);

    when(authUserRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(any(), any())).thenReturn(false);

    assertThrows(InvalidCredentialsException.class, () -> authUserService.loginUser(request));
  }
}
