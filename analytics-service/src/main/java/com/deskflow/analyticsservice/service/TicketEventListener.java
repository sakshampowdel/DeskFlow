package com.deskflow.analyticsservice.service;

import com.deskflow.analyticsservice.dto.event.EventEnvelope;
import com.deskflow.analyticsservice.dto.event.TicketCreatedEvent;
import com.deskflow.analyticsservice.dto.event.TicketResolvedEvent;
import com.deskflow.analyticsservice.dto.event.TicketUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketEventListener {

  private final AnalyticsService analyticsService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "TICKET_CREATED", groupId = "analytics-group")
  public void handleCreated(EventEnvelope<?> envelope) {
    TicketCreatedEvent ticketCreatedEvent =
        objectMapper.convertValue(envelope.payload(), TicketCreatedEvent.class);
    analyticsService.processTicketCreated(ticketCreatedEvent, envelope.occurredAt());
  }

  @KafkaListener(topics = "TICKET_UPDATED", groupId = "analytics-group")
  public void handleUpdated(EventEnvelope<?> envelope) {
    TicketUpdatedEvent ticketUpdatedEvent =
        objectMapper.convertValue(envelope.payload(), TicketUpdatedEvent.class);
    analyticsService.processTicketUpdated(ticketUpdatedEvent, envelope.occurredAt());
  }

  @KafkaListener(topics = "TICKET_RESOLVED", groupId = "analytics-group")
  public void handleResolved(EventEnvelope<?> envelope) {
    TicketResolvedEvent ticketResolvedEvent =
        objectMapper.convertValue(envelope.payload(), TicketResolvedEvent.class);
    analyticsService.processTicketResolved(ticketResolvedEvent, envelope.occurredAt());
  }
}
