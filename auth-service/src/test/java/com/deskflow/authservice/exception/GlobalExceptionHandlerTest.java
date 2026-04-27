package com.deskflow.authservice.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deskflow.authservice.controller.AuthUserController;
import com.deskflow.authservice.dto.request.LoginRequest;
import com.deskflow.authservice.dto.request.RegisterRequest;
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
class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AuthUserService authUserService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("Should return 400 with field errors on invalid request body")
  void handleValidation_ErrorResponse() throws Exception {
    var request = new RegisterRequest("not-an-email", "123");

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors.email").exists())
        .andExpect(jsonPath("$.errors.password").exists());
  }

  @Test
  @DisplayName("Should return 409 Conflict on duplicate email")
  void handleConflict_ErrorResponse() throws Exception {
    var request = new RegisterRequest("taken@test.com", "password123");
    when(authUserService.registerUser(any()))
        .thenThrow(new EmailAlreadyExistsException("Email taken"));

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").value("Email taken"));
  }

  @Test
  @DisplayName("Should return 401 Unauthorized on invalid credentials")
  void handleInvalidCredentials_ErrorResponse() throws Exception {
    var request = new LoginRequest("user@test.com", "wrongpass");
    when(authUserService.loginUser(any()))
        .thenThrow(new InvalidCredentialsException("Invalid credentials"));

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Invalid credentials"));
  }

  @Test
  @DisplayName("Should return 404 Not Found when user does not exist")
  void handleNotFound_ErrorResponse() throws Exception {
    var request = new LoginRequest("ghost@test.com", "pass123");
    when(authUserService.loginUser(any())).thenThrow(new UserNotFoundException("User missing"));

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("User missing"));
  }

  @Test
  @DisplayName("Should return 500 on unexpected runtime exception")
  void handleGeneric_ErrorResponse() throws Exception {
    var request = new LoginRequest("test@test.com", "pass123");
    when(authUserService.loginUser(any())).thenThrow(new RuntimeException("Database down"));

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }
}
