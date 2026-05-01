package com.deskflow.notificationservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deskflow.notificationservice.dto.request.PagedNotificationRequest;
import com.deskflow.notificationservice.dto.response.PagedNotificationResponse;
import com.deskflow.notificationservice.dto.response.UnreadCountResponse;
import com.deskflow.notificationservice.service.NotificationLogService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationLogControllerTest {

  private final String USER_ID = "user-123";
  @Autowired private MockMvc mockMvc;
  @MockitoBean private NotificationLogService notificationLogService;

  @Test
  @DisplayName("GET /notifications → 200 with paginated notifications")
  void getMyNotifications_ReturnsOk() throws Exception {
    var response = new PagedNotificationResponse(List.of(), 0, 10, 0L, 0, true);

    when(notificationLogService.getMyNotifications(
            eq(USER_ID), any(PagedNotificationRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/notifications")
                .header("X-User-Id", USER_ID)
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @DisplayName("PATCH /notifications/{id}/read → 204 No Content")
  void markAsRead_ReturnsNoContent() throws Exception {
    String notificationId = "note-abc";

    doNothing().when(notificationLogService).markAsRead(USER_ID, notificationId);

    mockMvc
        .perform(patch("/notifications/" + notificationId + "/read").header("X-User-Id", USER_ID))
        .andExpect(status().isNoContent());

    verify(notificationLogService).markAsRead(USER_ID, notificationId);
  }

  @Test
  @DisplayName("PATCH /notifications/read-all → 204 No Content")
  void markAllAsRead_ReturnsNoContent() throws Exception {
    doNothing().when(notificationLogService).markAllAsRead(USER_ID);

    mockMvc
        .perform(patch("/notifications/read-all").header("X-User-Id", USER_ID))
        .andExpect(status().isNoContent());

    verify(notificationLogService).markAllAsRead(USER_ID);
  }

  @Test
  @DisplayName("GET /notifications/unread-count → 200 with count")
  void getUnreadCount_ReturnsOk() throws Exception {
    var response = new UnreadCountResponse(5L);

    when(notificationLogService.getUnreadCount(USER_ID)).thenReturn(response);

    mockMvc
        .perform(get("/notifications/unread-count").header("X-User-Id", USER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count").value(5));
  }

  @Test
  @DisplayName("GET /notifications → 400 when pagination is invalid")
  void getMyNotifications_InvalidParams_ReturnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/notifications").header("X-User-Id", USER_ID).param("page", "-1"))
        .andExpect(status().isBadRequest());
  }
}
