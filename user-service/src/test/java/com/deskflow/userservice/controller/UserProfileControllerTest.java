package com.deskflow.userservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deskflow.userservice.dto.request.UpdateProfileRequest;
import com.deskflow.userservice.dto.response.InternalUserResponse;
import com.deskflow.userservice.dto.response.PagedUserResponse;
import com.deskflow.userservice.dto.response.UserProfileResponse;
import com.deskflow.userservice.model.Role;
import com.deskflow.userservice.service.UserProfileService;
import java.time.Instant;
import java.util.UUID;
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
@AutoConfigureMockMvc(addFilters = false)
class UserProfileControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final String USER_ID = UUID.randomUUID().toString();
  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserProfileService userProfileService;

  // ── /users/me ────────────────────────────────────────────────────────────

  @Test
  @DisplayName("GET /users/me → 200 using X-User-Id header")
  void getMyProfile_ReturnsOk() throws Exception {
    var response =
        new UserProfileResponse(
            USER_ID,
            "test@test.com",
            "John Doe",
            Role.SUBMITTER,
            "Engineering",
            null,
            true,
            Instant.now(),
            Instant.now());

    when(userProfileService.getMyProfile(USER_ID)).thenReturn(response);

    mockMvc
        .perform(get("/users/me").header("X-User-Id", USER_ID)) // Simulate Gateway injection
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(USER_ID))
        .andExpect(jsonPath("$.email").value("test@test.com"));
  }

  @Test
  @DisplayName("PATCH /users/me → 200 with updated profile")
  void updateMyProfile_ReturnsOk() throws Exception {
    var request = new UpdateProfileRequest("New Name", "HR", "http://avatar.url");
    var response =
        new UserProfileResponse(
            USER_ID,
            "test@test.com",
            "New Name",
            Role.SUBMITTER,
            "HR",
            "http://avatar.url",
            true,
            Instant.now(),
            Instant.now());

    when(userProfileService.updateMyProfile(eq(USER_ID), any())).thenReturn(response);

    mockMvc
        .perform(
            patch("/users/me")
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("New Name"))
        .andExpect(jsonPath("$.department").value("HR"));
  }

  // ── /users/{userId} ──────────────────────────────────────────────────────

  @Test
  @DisplayName("GET /users/{userId} → 200 for Agents/Admins")
  void getUserById_ReturnsOk() throws Exception {
    String targetId = UUID.randomUUID().toString();
    var response =
        new UserProfileResponse(
            targetId,
            "other@test.com",
            "Jane Smith",
            Role.SUBMITTER,
            "Finance",
            null,
            true,
            Instant.now(),
            Instant.now());

    when(userProfileService.getUserById(targetId)).thenReturn(response);

    mockMvc
        .perform(get("/users/" + targetId).header("X-User-Id", USER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(targetId));
  }

  // ── /users/{userId}/deactivate ───────────────────────────────────────────

  @Test
  @DisplayName("PATCH /users/{userId}/deactivate → 204 No Content")
  void deactivateUser_ReturnsNoContent() throws Exception {
    String targetId = UUID.randomUUID().toString();
    doNothing().when(userProfileService).deactivateUser(targetId);

    mockMvc
        .perform(patch("/users/" + targetId + "/deactivate").header("X-User-Id", USER_ID))
        .andExpect(status().isNoContent());

    verify(userProfileService).deactivateUser(targetId);
  }

  // ── /users/internal/{userId} ─────────────────────────────────────────────

  @Test
  @DisplayName("GET /users/internal/{userId} → 200 for internal services")
  void getInternalUserSummary_ReturnsOk() throws Exception {
    var response = new InternalUserResponse("sys@deskflow.com", "System User");

    when(userProfileService.getInternalUserSummary(USER_ID)).thenReturn(response);

    mockMvc
        .perform(get("/users/internal/" + USER_ID)) // No JWT/User header required
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("System User"))
        .andExpect(jsonPath("$.email").value("sys@deskflow.com"));
  }

  // ── /users (Pagination & Filtering) ──────────────────────────────────────

  @Test
  @DisplayName("GET /users → 200 with paginated results")
  void getAllUsers_ReturnsOk() throws Exception {
    var response =
        new PagedUserResponse(
            java.util.List.of(), // Empty content for simplicity
            0,
            20,
            0L,
            0);

    when(userProfileService.getAllUsers(any())).thenReturn(response);

    mockMvc
        .perform(
            get("/users")
                .header("X-User-Id", USER_ID)
                .param("page", "0")
                .param("size", "20")
                .param("role", "AGENT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20));

    verify(userProfileService).getAllUsers(any());
  }

  @Test
  @DisplayName("GET /users → 400 when pagination parameters are invalid")
  void getAllUsers_ReturnsBadRequest_WhenInvalid() throws Exception {
    mockMvc
        .perform(
            get("/users")
                .header("X-User-Id", USER_ID)
                .param("page", "-1") // Fails @Min(0)
                .param("size", "500")) // Fails @Max(100)
        .andExpect(status().isBadRequest());

    verifyNoInteractions(userProfileService);
  }
}
