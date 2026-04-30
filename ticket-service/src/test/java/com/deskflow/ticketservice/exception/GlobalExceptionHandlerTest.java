package com.deskflow.ticketservice.exception;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deskflow.ticketservice.controller.TicketController;
import com.deskflow.ticketservice.dto.request.CreateTicketRequest;
import com.deskflow.ticketservice.dto.request.UpdateStatusRequest;
import com.deskflow.ticketservice.model.Status;
import com.deskflow.ticketservice.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

  // Use findAndRegisterModules to avoid Date/Time issues in Spring Boot 4
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  @Autowired private MockMvc mockMvc;
  @MockitoBean private TicketService ticketService;

  @Test
  @DisplayName("Should return 404 Not Found when TicketNotFoundException is thrown")
  void handleTicketNotFound_Returns404() throws Exception {
    when(ticketService.getTicketById(anyString(), anyString(), anyString()))
        .thenThrow(new TicketNotFoundException("Ticket T-100 not found"));

    mockMvc
        .perform(
            get("/tickets/T-100").header("X-User-Id", "user-1").header("X-User-Role", "SUBMITTER"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Ticket T-100 not found"))
        .andExpect(jsonPath("$.error").value("Not Found"));
  }

  @Test
  @DisplayName("Should return 403 Forbidden when ForbiddenException is thrown")
  void handleForbidden_Returns403() throws Exception {
    when(ticketService.getTicketById(anyString(), anyString(), anyString()))
        .thenThrow(new ForbiddenException("Access denied"));

    mockMvc
        .perform(
            get("/tickets/T-100")
                .header("X-User-Id", "hacker-id")
                .header("X-User-Role", "SUBMITTER"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.message").value("Access denied"));
  }

  @Test
  @DisplayName("Should return 409 Conflict when InvalidStatusTransitionException is thrown")
  void handleInvalidTransition_Returns409() throws Exception {
    var request = new UpdateStatusRequest(Status.OPEN, null);

    doThrow(new InvalidStatusTransitionException("Cannot move from CLOSED to OPEN"))
        .when(ticketService)
        .updateTicketStatus(anyString(), anyString(), anyString(), any());

    mockMvc
        .perform(
            patch("/tickets/T-100/status")
                .header("X-User-Id", "agent-1")
                .header("X-User-Role", "AGENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").value("Cannot move from CLOSED to OPEN"));
  }

  @Test
  @DisplayName("Should return 400 Bad Request on MethodArgumentNotValidException (Bean Validation)")
  void handleValidation_Returns400WithErrors() throws Exception {
    // Title is @NotBlank, so empty string should trigger MethodArgumentNotValidException
    var invalidRequest = new CreateTicketRequest("", "Desc", null, null);

    mockMvc
        .perform(
            post("/tickets")
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.errors.title").exists());
  }

  @Test
  @DisplayName("Should return 500 Internal Server Error on unhandled Exception")
  void handleGeneric_Returns500() throws Exception {
    when(ticketService.getTicketById(anyString(), anyString(), anyString()))
        .thenThrow(new RuntimeException("S3 connection failed"));

    mockMvc
        .perform(get("/tickets/T-100").header("X-User-Id", "user-1").header("X-User-Role", "AGENT"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }
}
