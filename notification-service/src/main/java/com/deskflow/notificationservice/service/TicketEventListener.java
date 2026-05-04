package com.deskflow.notificationservice.service;

import com.deskflow.notificationservice.dto.event.*;
import com.deskflow.notificationservice.model.KafkaEventType;
import com.deskflow.notificationservice.model.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketEventListener {

  private final NotificationLogService notificationLogService;

  @KafkaListener(topics = "TICKET_CREATED", groupId = "notification-group")
  public void handleCreated(EventEnvelope<TicketCreatedEvent> envelope) {
    TicketCreatedEvent ticketCreatedEvent = envelope.payload();
    notificationLogService.createAndPush(
        ticketCreatedEvent.reporterId(),
        Type.WEBSOCKET_PUSH,
        KafkaEventType.TICKET_CREATED,
        "Ticket Created",
        "Ticket '" + ticketCreatedEvent.title() + "' was successfully submitted.",
        ticketCreatedEvent.ticketId());
  }

  @KafkaListener(topics = "TICKET_UPDATED", groupId = "notification-group")
  public void handleUpdated(EventEnvelope<TicketUpdatedEvent> envelope) {
    TicketUpdatedEvent ticketUpdatedEvent = envelope.payload();
    notificationLogService.createAndPush(
        ticketUpdatedEvent.reporterId(),
        Type.WEBSOCKET_PUSH,
        KafkaEventType.TICKET_UPDATED,
        "Ticket Updated",
        "Ticket '" + ticketUpdatedEvent.ticketId() + "' was successfully submitted.",
        ticketUpdatedEvent.ticketId());
  }

  @KafkaListener(topics = "TICKET_ASSIGNED", groupId = "notification-group")
  public void handleAssigned(EventEnvelope<TicketAssignedEvent> envelope) {
    TicketAssignedEvent ticketAssignedEvent = envelope.payload();
    notificationLogService.createAndPush(
        ticketAssignedEvent.assigneeId(),
        Type.WEBSOCKET_PUSH,
        KafkaEventType.TICKET_ASSIGNED,
        "Ticket Assigned",
        "Ticket '" + ticketAssignedEvent.title() + "' has been assigned to you.",
        ticketAssignedEvent.ticketId());
  }

  @KafkaListener(topics = "TICKET_RESOLVED", groupId = "notification-group")
  public void handleResolved(EventEnvelope<TicketResolvedEvent> envelope) {
    TicketResolvedEvent ticketResolvedEvent = envelope.payload();
    notificationLogService.createAndPush(
        ticketResolvedEvent.reporterId(),
        Type.WEBSOCKET_PUSH,
        KafkaEventType.TICKET_RESOLVED,
        "Ticket Resolved",
        "Ticket '" + ticketResolvedEvent.title() + "' is resolved.",
        ticketResolvedEvent.ticketId());
  }
}
