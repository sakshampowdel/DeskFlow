package com.deskflow.userservice.exception;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deskflow.userservice.controller.UserProfileController;
import com.deskflow.userservice.service.UserProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserProfileController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypasses security filters for unit testing
class GlobalExceptionHandlerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserProfileService userProfileService;

  @Test
  @DisplayName("Should return 404 Not Found on UserNotFoundException")
  void handleNotFound_Returns404() throws Exception {
    when(userProfileService.getUserById(anyString()))
        .thenThrow(new UserNotFoundException("User not found"));

    mockMvc
        .perform(
            get("/users/some-id")
                .header("X-User-Id", "test-caller-id")) // FIXED: Added missing header
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("User not found"));
  }

  @Test
  @DisplayName("Should return 500 Internal Server Error on generic Exception")
  void handleGeneric_Returns500() throws Exception {
    // Arrange
    when(userProfileService.getMyProfile(anyString()))
        .thenThrow(new RuntimeException("Database connection failed"));

    // Act & Assert
    mockMvc
        .perform(get("/users/me").header("X-User-Id", "test-uuid"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }
}
