package com.deskflow.notificationservice.exception;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deskflow.notificationservice.controller.NotificationLogController;
import com.deskflow.notificationservice.service.NotificationLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

  private final String USER_ID = "user-123";
  @Autowired private MockMvc mockMvc;
  @MockitoBean private NotificationLogService notificationLogService;

  @Test
  @DisplayName("Should return 404 Not Found when NotificationNotFoundException is thrown")
  void handleNotificationNotFound_Returns404() throws Exception {
    when(notificationLogService.getUnreadCount(USER_ID))
        .thenThrow(new NotificationNotFoundException("Notification not found"));

    mockMvc
        .perform(get("/notifications/unread-count").header("X-User-Id", USER_ID))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Notification not found"))
        .andExpect(jsonPath("$.error").value("Not Found"));
  }

  @Test
  @DisplayName("Should return 403 Forbidden when ForbiddenException is thrown")
  void handleForbidden_Returns403() throws Exception {
    doThrow(new ForbiddenException("Access denied"))
        .when(notificationLogService)
        .markAsRead(eq(USER_ID), anyString());

    mockMvc
        .perform(patch("/notifications/note-123/read").header("X-User-Id", USER_ID))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Access denied"));
  }

  @Test
  @DisplayName("Should return 500 Internal Server Error on generic Exception")
  void handleGeneric_Returns500() throws Exception {
    when(notificationLogService.getUnreadCount(USER_ID))
        .thenThrow(new RuntimeException("Database failure"));

    mockMvc
        .perform(get("/notifications/unread-count").header("X-User-Id", USER_ID))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }

  @Test
  @DisplayName("Should return 400 Bad Request on MethodArgumentNotValidException")
  void handleValidation_Returns400() throws Exception {
    // Testing the @Valid constraint on the controller's @ModelAttribute
    mockMvc
        .perform(
            get("/notifications").header("X-User-Id", USER_ID).param("page", "-1")) // Fails @Min(0)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").value("Validation failed"));
  }
}
