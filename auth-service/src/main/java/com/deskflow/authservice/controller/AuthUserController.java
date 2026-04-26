package com.deskflow.authservice.controller;

import com.deskflow.authservice.dto.reponse.AuthTokenResponse;
import com.deskflow.authservice.dto.reponse.RegisterResponse;
import com.deskflow.authservice.dto.request.*;
import com.deskflow.authservice.service.AuthUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthUserController {
  private final AuthUserService authUserService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
      @Valid @RequestBody RegisterRequest registerRequest) {
    RegisterResponse response = authUserService.registerUser(registerRequest);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    AuthTokenResponse response = authUserService.loginUser(loginRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthTokenResponse> refresh(
      @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
    return ResponseEntity.ok(authUserService.refreshUser(refreshTokenRequest));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest refreshTokenRequest) {
    authUserService.logoutUser(refreshTokenRequest);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/password")
  public ResponseEntity<Void> updatePassword(
      @RequestHeader("X-User-Id") String userId,
      @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
    authUserService.updateUserPassword(userId, changePasswordRequest);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/role/{userId}")
  public ResponseEntity<Void> updateRole(
      @RequestHeader("X-User-Id") String adminId,
      @PathVariable String userId,
      @Valid @RequestBody ChangeRoleRequest changeRoleRequest) {
    authUserService.updateUserRole(userId, changeRoleRequest);
    return ResponseEntity.noContent().build();
  }
}
