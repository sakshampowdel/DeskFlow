package com.deskflow.authservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deskflow.authservice.dto.reponse.AuthTokenResponse;
import com.deskflow.authservice.dto.reponse.RegisterResponse;
import com.deskflow.authservice.dto.request.*;
import com.deskflow.authservice.model.Role;
import com.deskflow.authservice.service.AuthUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthUserControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthUserService authUserService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  // ── /auth/register ────────────────────────────────────────────────────────

  @Test
  @DisplayName("POST /auth/register → 201 with registration response")
  void register_ReturnsCreated() throws Exception {
    var request = new RegisterRequest("user@test.com", "password123");
    var response = new RegisterResponse("user-id-1", "user@test.com", Role.SUBMITTER);
    when(authUserService.registerUser(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("user-id-1"))
        .andExpect(jsonPath("$.email").value("user@test.com"))
        .andExpect(jsonPath("$.role").value("SUBMITTER"));
  }

  @Test
  @DisplayName("POST /auth/register → 400 on invalid request body")
  void register_ReturnsBadRequest_WhenInvalid() throws Exception {
    var request = new RegisterRequest("not-an-email", "short");

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(authUserService);
  }

  // ── /auth/login ───────────────────────────────────────────────────────────

  @Test
  @DisplayName("POST /auth/login → 200 with token response")
  void login_ReturnsOk() throws Exception {
    var request = new LoginRequest("user@test.com", "password123");
    var response = new AuthTokenResponse("access-token", "refresh-token", 3600);
    when(authUserService.loginUser(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
        .andExpect(jsonPath("$.expiresIn").value(3600));
  }

  @Test
  @DisplayName("POST /auth/login → 400 on invalid request body")
  void login_ReturnsBadRequest_WhenInvalid() throws Exception {
    var request = new LoginRequest("not-an-email", "");

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(authUserService);
  }

  // ── /auth/refresh ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("POST /auth/refresh → 200 with new token response")
  void refresh_ReturnsOk() throws Exception {
    var request = new RefreshTokenRequest("valid-refresh-token");
    var response = new AuthTokenResponse("new-access-token", "new-refresh-token", 3600);
    when(authUserService.refreshUser(any())).thenReturn(response);

    mockMvc
        .perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new-access-token"))
        .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
  }

  @Test
  @DisplayName("POST /auth/refresh → 400 when refresh token is blank")
  void refresh_ReturnsBadRequest_WhenBlank() throws Exception {
    var request = new RefreshTokenRequest("");

    mockMvc
        .perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(authUserService);
  }

  // ── /auth/logout ──────────────────────────────────────────────────────────

  @Test
  @DisplayName("POST /auth/logout → 204 No Content")
  void logout_ReturnsNoContent() throws Exception {
    var request = new RefreshTokenRequest("valid-refresh-token");
    doNothing().when(authUserService).logoutUser(any());

    mockMvc
        .perform(
            post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(authUserService).logoutUser(any());
  }

  // ── /auth/password ────────────────────────────────────────────────────────

  @Test
  @DisplayName("PATCH /auth/password → 204 No Content")
  void updatePassword_ReturnsNoContent() throws Exception {
    var request = new ChangePasswordRequest("oldPassword1", "newPassword1");
    doNothing().when(authUserService).updateUserPassword(any(), any());

    mockMvc
        .perform(
            patch("/auth/password")
                .header("X-User-Id", "user-id-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(authUserService).updateUserPassword(eq("user-id-1"), any());
  }

  @Test
  @DisplayName("PATCH /auth/password → 400 when new password is too short")
  void updatePassword_ReturnsBadRequest_WhenInvalid() throws Exception {
    var request = new ChangePasswordRequest("oldPassword1", "short");

    mockMvc
        .perform(
            patch("/auth/password")
                .header("X-User-Id", "user-id-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(authUserService);
  }

  // ── /auth/role/{userId} ───────────────────────────────────────────────────

  @Test
  @DisplayName("PATCH /auth/role/{userId} → 204 No Content")
  void updateRole_ReturnsNoContent() throws Exception {
    var request = new ChangeRoleRequest(Role.ADMIN);
    doNothing().when(authUserService).updateUserRole(any(), any());

    mockMvc
        .perform(
            patch("/auth/role/target-user-id")
                .header("X-User-Id", "admin-id-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(authUserService).updateUserRole(eq("target-user-id"), any());
  }

  @Test
  @DisplayName("PATCH /auth/role/{userId} → 400 when role is null")
  void updateRole_ReturnsBadRequest_WhenRoleNull() throws Exception {
    var request = new ChangeRoleRequest(null);

    mockMvc
        .perform(
            patch("/auth/role/target-user-id")
                .header("X-User-Id", "admin-id-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(authUserService);
  }
}
