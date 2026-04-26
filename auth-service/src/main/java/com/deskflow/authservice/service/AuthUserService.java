package com.deskflow.authservice.service;

import com.deskflow.authservice.dto.reponse.AuthTokenResponse;
import com.deskflow.authservice.dto.reponse.RegisterResponse;
import com.deskflow.authservice.dto.request.*;
import com.deskflow.authservice.model.AuthUser;
import com.deskflow.authservice.model.RefreshToken;
import com.deskflow.authservice.model.Role;
import com.deskflow.authservice.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserService {

  private final AuthUserRepository authUserRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;

  private RegisterResponse mapToRegisterResponse(AuthUser authUser) {
    return new RegisterResponse(authUser.getId(), authUser.getEmail(), authUser.getRole());
  }

  private AuthTokenResponse mapToAuthTokenResponse(AuthUser authUser, RefreshToken refreshToken) {
    return new AuthTokenResponse(
        jwtService.generateAccessToken(authUser),
        refreshToken.getToken(),
        (int) jwtService.getExpiration());
  }

  private AuthTokenResponse mapToAuthTokenResponse(AuthUser authUser) {
    return this.mapToAuthTokenResponse(
        authUser, refreshTokenService.createRefreshToken(authUser.getId()));
  }

  private AuthUser findUserById(String userId) {
    return authUserRepository
        .findById(userId)
        .orElseThrow(() -> new RuntimeException("User " + userId + " not found"));
  }

  public RegisterResponse registerUser(RegisterRequest registerRequest) {
    // Email already exists
    if (authUserRepository.existsByEmail(registerRequest.email())) {
      throw new RuntimeException("Email already exists");
    }

    String email = registerRequest.email();
    String password = passwordEncoder.encode(registerRequest.password());
    Role role = Role.SUBMITTER;

    AuthUser saved = authUserRepository.save(new AuthUser(email, password, role));

    return mapToRegisterResponse(saved);
  }

  public AuthTokenResponse loginUser(LoginRequest loginRequest) {
    AuthUser user =
        authUserRepository
            .findByEmail(loginRequest.email())
            .orElseThrow(() -> new RuntimeException("Email does not exist"));

    if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
      throw new RuntimeException("Wrong password");
    }

    return mapToAuthTokenResponse(user);
  }

  public AuthTokenResponse refreshUser(RefreshTokenRequest refreshTokenRequest) {
    RefreshToken refreshToken = refreshTokenService.validate(refreshTokenRequest.refreshToken());
    AuthUser user = findUserById(refreshToken.getUserId());
    return mapToAuthTokenResponse(user, refreshToken);
  }

  public void logoutUser(RefreshTokenRequest refreshTokenRequest) {
    refreshTokenService.revoke(refreshTokenRequest.refreshToken());
  }

  public void updateUserPassword(String userId, ChangePasswordRequest changePasswordRequest) {
    AuthUser user = findUserById(userId);

    if (!passwordEncoder.matches(changePasswordRequest.currentPassword(), user.getPasswordHash())) {
      throw new RuntimeException("Wrong password");
    }

    user.setPasswordHash(passwordEncoder.encode(changePasswordRequest.newPassword()));
    authUserRepository.save(user);
  }

  public void updateUserRole(String userId, ChangeRoleRequest changeRoleRequest) {
    AuthUser user = findUserById(userId);
    user.setRole(changeRoleRequest.role());
    authUserRepository.save(user);
  }
}
