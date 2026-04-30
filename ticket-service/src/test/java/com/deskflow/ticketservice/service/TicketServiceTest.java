package com.deskflow.ticketservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.deskflow.ticketservice.dto.request.*;
import com.deskflow.ticketservice.dto.response.TicketResponse;
import com.deskflow.ticketservice.exception.*;
import com.deskflow.ticketservice.model.*;
import com.deskflow.ticketservice.repository.TicketRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

  @Mock private TicketRepository ticketRepository;

  @InjectMocks private TicketService ticketService;

  @Test
  @DisplayName("Create ticket should calculate correct SLA based on priority")
  void createTicket_validRequest_calculatesSla() {
    String reporterId = "user-123";
    CreateTicketRequest request =
        new CreateTicketRequest(
            "Printer Broken", "Won't turn on", Priority.URGENT, Category.HARDWARE);

    when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArguments()[0]);

    TicketResponse response = ticketService.createTicket(reporterId, request);

    assertThat(response.priority()).isEqualTo(Priority.URGENT);
    assertThat(response.slaDeadline()).isAfter(Instant.now().plusSeconds(3 * 3600));
    assertThat(response.status()).isEqualTo(Status.OPEN);
    verify(ticketRepository).save(any(Ticket.class));
  }

  @Test
  @DisplayName("Update status should throw exception for invalid transitions")
  void updateTicketStatus_invalidTransition_throwsException() {
    String ticketId = "ticket-1";
    Ticket ticket = new Ticket("Title", "Desc", Priority.LOW, Category.OTHER, "rep", Instant.now());
    ticket.setStatus(Status.CLOSED);

    when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
    UpdateStatusRequest request = new UpdateStatusRequest(Status.OPEN, null);

    assertThrows(
        InvalidStatusTransitionException.class,
        () -> ticketService.updateTicketStatus("admin", "ADMIN", ticketId, request));
  }

  @Test
  @DisplayName("Update status to RESOLVED requires a resolution note")
  void updateTicketStatus_toResolvedWithoutNote_throwsException() {
    String ticketId = "ticket-1";
    Ticket ticket = new Ticket("Title", "Desc", Priority.LOW, Category.OTHER, "rep", Instant.now());
    ticket.setStatus(Status.IN_PROGRESS);

    when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
    UpdateStatusRequest request = new UpdateStatusRequest(Status.RESOLVED, "");

    assertThrows(
        ValidationException.class,
        () -> ticketService.updateTicketStatus("admin", "ADMIN", ticketId, request));
  }

  @Test
  @DisplayName("Add comment should fail if SUBMITTER tries to make it internal")
  void addComment_submitterInternalComment_throwsForbidden() {
    AddCommentRequest request = new AddCommentRequest("Hidden message", true);

    assertThrows(
        ForbiddenException.class,
        () -> ticketService.addComment("user-1", "SUBMITTER", "ticket-1", request));

    verify(ticketRepository, never()).save(any());
  }

  @Test
  @DisplayName(
      "Get ticket should throw Forbidden if Submitter tries to access someone else's ticket")
  void getTicketById_unauthorizedSubmitter_throwsForbidden() {
    String ticketId = "ticket-1";
    Ticket ticket =
        new Ticket("Title", "Desc", Priority.LOW, Category.OTHER, "actual-reporter", Instant.now());

    when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

    assertThrows(
        ForbiddenException.class,
        () -> ticketService.getTicketById("hacker-id", "SUBMITTER", ticketId));
  }

  @Test
  @DisplayName("Update priority should recalculate SLA deadline")
  void updateTicketPriority_validRequest_updatesSla() {
    String ticketId = "T-100";
    Instant baseline = Instant.now().plus(10, ChronoUnit.DAYS);

    Ticket ticket = new Ticket("Title", "Desc", Priority.LOW, Category.OTHER, "rep", baseline);
    ReflectionTestUtils.setField(ticket, "id", ticketId);

    when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
    UpdatePriorityRequest request = new UpdatePriorityRequest(Priority.URGENT);

    ticketService.updateTicketPriority("agent-1", ticketId, request);

    assertThat(ticket.getPriority()).isEqualTo(Priority.URGENT);
    assertThat(ticket.getSlaDeadline()).isBefore(baseline.minus(5, ChronoUnit.DAYS));
    verify(ticketRepository).save(ticket);
  }
}
