package com.deskflow.userservice.exception;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deskflow.userservice.controller.UserProfileController;
import com.deskflow.userservice.dto.request.UpdateProfileRequest;
import com.deskflow.userservice.service.UserProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
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
    // Arrange: Mock service to throw the domain exception
    when(userProfileService.getUserById(anyString()))
        .thenThrow(new UserNotFoundException("User not found"));

    // Act & Assert
    mockMvc
        .perform(get("/users/some-id"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("User not found"));
  }

  @Test
  @DisplayName("Should return 400 Bad Request on validation failure")
  void handleValidation_Returns400() throws Exception {
    // Arrange: Create a request that fails @Valid constraints (e.g., if fullName is @NotBlank)
    // Note: For this test to trigger MethodArgumentNotValidException,
    // the UpdateProfileRequest DTO must have validation annotations.
    var invalidRequest = new UpdateProfileRequest("", null, "invalid-url");

    // Act & Assert
    mockMvc
        .perform(
            patch("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .header("X-User-Id", "test-uuid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors").exists());
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
