package com.deskflow.ticketservice.service;

import com.deskflow.ticketservice.dto.request.*;
import com.deskflow.ticketservice.dto.response.*;
import com.deskflow.ticketservice.exception.*;
import com.deskflow.ticketservice.model.*;
import com.deskflow.ticketservice.repository.TicketRepository;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TicketService {

  private static final Map<Status, Set<Status>> ALLOWED_TRANSITIONS =
      Map.of(
          Status.OPEN, EnumSet.of(Status.IN_PROGRESS, Status.CLOSED),
          Status.IN_PROGRESS, EnumSet.of(Status.ON_HOLD, Status.RESOLVED, Status.CLOSED),
          Status.ON_HOLD, EnumSet.of(Status.IN_PROGRESS, Status.CLOSED),
          Status.RESOLVED, EnumSet.of(Status.CLOSED),
          Status.CLOSED, EnumSet.noneOf(Status.class));

  private final TicketRepository ticketRepository;

  private Ticket findById(String ticketId) {
    return ticketRepository
        .findById(ticketId)
        .orElseThrow(() -> new TicketNotFoundException("Ticket not found"));
  }

  private Instant calculateSlaDeadline(Priority priority) {
    long hours =
        switch (priority) {
          case LOW -> 72;
          case MEDIUM -> 48;
          case HIGH -> 24;
          case URGENT -> 4;
        };
    return Instant.now().plusSeconds(hours * 3600);
  }

  private TicketCommentResponse mapToCommentResponse(TicketComment comment) {
    return new TicketCommentResponse(
        comment.getId(),
        comment.getAuthorId(),
        comment.getBody(),
        comment.isInternal(),
        comment.getCreatedAt());
  }

  private TicketResponse mapToTicketResponse(Ticket ticket) {
    return new TicketResponse(
        ticket.getId(),
        ticket.getTitle(),
        ticket.getDescription(),
        ticket.getStatus(),
        ticket.getPriority(),
        ticket.getCategory(),
        ticket.getReporterId(),
        ticket.getAssigneeId(),
        ticket.getAttachmentUrls(),
        ticket.getSlaDeadline(),
        ticket.isSlaBreached(),
        ticket.getResolutionNote(),
        ticket.getComments().stream().map(this::mapToCommentResponse).toList(),
        ticket.getCreatedAt(),
        ticket.getUpdatedAt());
  }

  private TicketSummaryResponse mapToSummaryResponse(Ticket ticket) {
    return new TicketSummaryResponse(
        ticket.getId(),
        ticket.getTitle(),
        ticket.getStatus(),
        ticket.getPriority(),
        ticket.getCategory(),
        ticket.getReporterId(),
        ticket.getAssigneeId(),
        ticket.getSlaDeadline(),
        ticket.isSlaBreached(),
        ticket.getCreatedAt());
  }

  public TicketResponse createTicket(String reporterId, CreateTicketRequest request) {
    Ticket ticket =
        new Ticket(
            request.title(),
            request.description(),
            request.priority(),
            request.category(),
            reporterId,
            calculateSlaDeadline(request.priority()));
    return mapToTicketResponse(ticketRepository.save(ticket));
  }

  public PagedTicketResponse getAllTickets(PagedTicketRequest request) {
    Ticket probe = new Ticket();
    if (request.status() != null) probe.setStatus(request.status());
    if (request.priority() != null) probe.setPriority(request.priority());
    if (request.category() != null) probe.setCategory(request.category());
    if (request.assigneeId() != null) probe.setAssigneeId(request.assigneeId());
    if (request.slaBreached() != null) probe.setSlaBreached(request.slaBreached());

    ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
    Example<Ticket> example = Example.of(probe, matcher);

    Page<Ticket> page =
        ticketRepository.findAll(example, PageRequest.of(request.page(), request.size()));

    return new PagedTicketResponse(
        page.getContent().stream().map(this::mapToSummaryResponse).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isLast());
  }

  public PagedTicketResponse getMyTickets(String userId, PagedTicketRequest request) {
    Page<Ticket> page =
        ticketRepository.findByReporterId(userId, PageRequest.of(request.page(), request.size()));
    return new PagedTicketResponse(
        page.getContent().stream().map(this::mapToSummaryResponse).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isLast());
  }

  public TicketResponse getTicketById(String requesterId, String requesterRole, String ticketId) {
    Ticket ticket = findById(ticketId);
    if ("SUBMITTER".equals(requesterRole) && !ticket.getReporterId().equals(requesterId)) {
      throw new ForbiddenException("Access denied");
    }
    return mapToTicketResponse(ticket);
  }

  public void updateTicketStatus(
      String updaterId, String updaterRole, String ticketId, UpdateStatusRequest request) {
    Ticket ticket = findById(ticketId);
    Status current = ticket.getStatus();
    Status next = request.status();

    if (!ALLOWED_TRANSITIONS.get(current).contains(next)) {
      throw new InvalidStatusTransitionException("Invalid status transition");
    }
    if (next == Status.RESOLVED) {
      if (request.resolutionNote() == null || request.resolutionNote().isBlank()) {
        throw new ValidationException("Invalid request");
      }
      ticket.setResolutionNote(request.resolutionNote());
    }

    ticket.setStatus(next);
    ticketRepository.save(ticket);
  }

  public void assignTicket(
      String assignedById, String assignedByRole, String ticketId, AssignTicketRequest request) {
    Ticket ticket = findById(ticketId);
    ticket.setAssigneeId(request.assigneeId());
    ticketRepository.save(ticket);
  }

  public void updateTicketPriority(
      String updaterId, String ticketId, UpdatePriorityRequest request) {
    Ticket ticket = findById(ticketId);
    ticket.setPriority(request.priority());
    ticket.setSlaDeadline(calculateSlaDeadline(request.priority()));
    ticketRepository.save(ticket);
  }

  public TicketCommentResponse addComment(
      String commenterId, String commenterRole, String ticketId, AddCommentRequest request) {
    if ("SUBMITTER".equals(commenterRole) && request.isInternal()) {
      throw new ForbiddenException("Access denied");
    }
    Ticket ticket = findById(ticketId);
    TicketComment comment = new TicketComment(commenterId, request.body(), request.isInternal());
    ticket.getComments().add(comment);
    ticketRepository.save(ticket);
    return mapToCommentResponse(comment);
  }

  public List<TicketCommentResponse> getComments(
      String requesterId, String requesterRole, String ticketId) {
    Ticket ticket = findById(ticketId);
    return ticket.getComments().stream()
        .filter(c -> !c.isInternal() || !"SUBMITTER".equals(requesterRole))
        .map(this::mapToCommentResponse)
        .toList();
  }

  public AttachmentResponse uploadAttachment(
      String uploaderId, String ticketId, MultipartFile file) {
    Ticket ticket = findById(ticketId);
    if (ticket.getAttachmentUrls().size() >= 5) {
      throw new ValidationException("Attachment limit reached");
    }
    // TODO: upload to LocalStack S3 and replace with real URL
    String url = "s3://placeholder/" + file.getOriginalFilename();
    ticket.getAttachmentUrls().add(url);
    ticketRepository.save(ticket);
    return new AttachmentResponse(url);
  }
}
