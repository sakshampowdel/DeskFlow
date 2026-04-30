package com.deskflow.ticketservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.deskflow.ticketservice.dto.request.*;
import com.deskflow.ticketservice.dto.response.*;
import com.deskflow.ticketservice.model.*;
import com.deskflow.ticketservice.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

  // Manual ObjectMapper setup to ensure Java 8 Time support for Instants
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  private final String USER_ID = "user-123";
  private final String USER_ROLE = "SUBMITTER";
  @Autowired private MockMvc mockMvc;
  @MockitoBean private TicketService ticketService;

  @Test
  @DisplayName("POST /tickets → 201 Created")
  void createTicket_ReturnsCreated() throws Exception {
    var request =
        new CreateTicketRequest(
            "Broken Laptop", "Screen is flickering", Priority.HIGH, Category.HARDWARE);
    var response =
        new TicketResponse(
            "T-1",
            "Broken Laptop",
            "Screen",
            Status.OPEN,
            Priority.HIGH,
            Category.HARDWARE,
            USER_ID,
            null,
            List.of(),
            Instant.now(),
            false,
            null,
            List.of(),
            Instant.now(),
            Instant.now());

    when(ticketService.createTicket(eq(USER_ID), any(CreateTicketRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/tickets")
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("T-1"))
        .andExpect(jsonPath("$.title").value("Broken Laptop"));
  }

  @Test
  @DisplayName("GET /tickets/me → 200 with paginated tickets")
  void getMyTickets_ReturnsOk() throws Exception {
    var response = new PagedTicketResponse(List.of(), 0, 10, 0L, 0, true);

    // Note: Spring maps query params to the @ModelAttribute PagedTicketRequest automatically
    when(ticketService.getMyTickets(eq(USER_ID), any(PagedTicketRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/tickets/me").header("X-User-Id", USER_ID).param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.last").value(true));
  }

  @Test
  @DisplayName("PATCH /tickets/{id}/status → 204 No Content")
  void updateStatus_ReturnsNoContent() throws Exception {
    String ticketId = UUID.randomUUID().toString();
    var request = new UpdateStatusRequest(Status.IN_PROGRESS, null);

    doNothing()
        .when(ticketService)
        .updateTicketStatus(eq(USER_ID), eq("AGENT"), eq(ticketId), any());

    mockMvc
        .perform(
            patch("/tickets/" + ticketId + "/status")
                .header("X-User-Id", USER_ID)
                .header("X-User-Role", "AGENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("POST /tickets/{id}/comments → 201 Created")
  void addComment_ReturnsCreated() throws Exception {
    String ticketId = "T-1";
    var request = new AddCommentRequest("Working on this now", false);
    var response =
        new TicketCommentResponse("C-1", USER_ID, "Working on this now", false, Instant.now());

    when(ticketService.addComment(eq(USER_ID), eq(USER_ROLE), eq(ticketId), any()))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/tickets/" + ticketId + "/comments")
                .header("X-User-Id", USER_ID)
                .header("X-User-Role", USER_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.body").value("Working on this now"));
  }

  @Test
  @DisplayName("POST /tickets/{id}/attachments → 201 Created")
  void uploadAttachment_ReturnsCreated() throws Exception {
    String ticketId = "T-1";
    MockMultipartFile file =
        new MockMultipartFile("file", "test.png", MediaType.IMAGE_PNG_VALUE, "content".getBytes());

    // Ensure the field name here matches your AttachmentResponse record field
    when(ticketService.uploadAttachment(eq("user-1"), eq(ticketId), any()))
        .thenReturn(new AttachmentResponse("http://s3/url"));

    mockMvc
        .perform(
            multipart("/tickets/" + ticketId + "/attachments")
                .file(file)
                .header("X-User-Id", "user-1"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.attachmentUrl").value("http://s3/url"));
  }

  @Test
  @DisplayName("GET /tickets/ → Should handle missing ID")
  void getTicket_InvalidPath_ReturnsError() throws Exception {
    mockMvc
        .perform(get("/tickets/").header("X-User-Id", "user-1").header("X-User-Role", "SUBMITTER"))
        .andExpect(
            status().isInternalServerError()); // Matches GlobalExceptionHandler generic catch-all
  }
}
