package com.deskflow.ticketservice.controller;

import com.deskflow.ticketservice.dto.request.*;
import com.deskflow.ticketservice.dto.response.AttachmentResponse;
import com.deskflow.ticketservice.dto.response.PagedTicketResponse;
import com.deskflow.ticketservice.dto.response.TicketCommentResponse;
import com.deskflow.ticketservice.dto.response.TicketResponse;
import com.deskflow.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Validated
public class TicketController {

  private final TicketService ticketService;

  @PostMapping
  public ResponseEntity<TicketResponse> createTicket(
      @RequestHeader("X-User-Id") String reporterId,
      @Valid @RequestBody CreateTicketRequest createTicketRequest) {
    return null;
  }

  @GetMapping
  public ResponseEntity<PagedTicketResponse> getAllTickets(
      @RequestHeader("X-User-Id") String requesterId,
      @RequestHeader("X-User-Role") String requesterRole,
      @ModelAttribute @Valid PagedTicketRequest pagedTicketRequest) {
    return null;
  }

  @GetMapping("/me")
  public ResponseEntity<PagedTicketResponse> getMyTickets(
      @RequestHeader("X-User-Id") String userId,
      @ModelAttribute @Valid PagedTicketRequest pagedTicketRequest) {
    return null;
  }

  @GetMapping("/{ticketId}")
  public ResponseEntity<TicketResponse> getTicketById(
      @RequestHeader("X-User-Id") String requesterId,
      @RequestHeader("X-User-Role") String requesterRole,
      @PathVariable String ticketId) {
    return null;
  }

  @PatchMapping("/{ticketId}/status")
  public ResponseEntity<Void> updateTicketStatus(
      @RequestHeader("X-User-Id") String updaterId,
      @RequestHeader("X-User-Role") String updaterRole,
      @Valid @RequestBody UpdateStatusRequest updateStatusRequest,
      @PathVariable String ticketId) {
    return null;
  }

  @PatchMapping("/{ticketId}/assign")
  public ResponseEntity<Void> assignTicket(
      @RequestHeader("X-User-Id") String assignedById,
      @RequestHeader("X-User-Role") String assignedByRole,
      @Valid @RequestBody AssignTicketRequest assignTicketRequest,
      @PathVariable String ticketId) {
    return null;
  }

  @PatchMapping("/{ticketId}/priority")
  public ResponseEntity<Void> updateTicketPriority(
      @RequestHeader("X-User-Id") String updaterId,
      @RequestHeader("X-User-Role") String updaterRole,
      @Valid @RequestBody UpdatePriorityRequest updatePriorityRequest,
      @PathVariable String ticketId) {
    return null;
  }

  @PostMapping("/{ticketId}/comments")
  public ResponseEntity<TicketCommentResponse> addComment(
      @RequestHeader("X-User-Id") String commenterId,
      @RequestHeader("X-User-Role") String commenterRole,
      @Valid @RequestBody AddCommentRequest addCommentRequest,
      @PathVariable String ticketId) {
    return null;
  }

  @GetMapping("/{ticketId}/comments")
  public ResponseEntity<List<TicketCommentResponse>> getComments(
      @RequestHeader("X-User-Id") String requesterId,
      @RequestHeader("X-User-Role") String requesterRole,
      @PathVariable String ticketId) {
    return null;
  }

  @PostMapping("/{ticketId}/attachments")
  public ResponseEntity<AttachmentResponse> uploadAttachment(
      @RequestHeader("X-User-Id") String uploaderId,
      @RequestParam("file") MultipartFile file,
      @PathVariable String ticketId) {
    return null;
  }
}
