package com.deskflow.analyticsservice.service;

import com.deskflow.analyticsservice.dto.event.EventEnvelope;
import com.deskflow.analyticsservice.dto.event.TicketCreatedEvent;
import com.deskflow.analyticsservice.dto.event.TicketResolvedEvent;
import com.deskflow.analyticsservice.dto.event.TicketUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketEventListener {

  private final AnalyticsService analyticsService;

  @KafkaListener(topics = "TICKET_CREATED", groupId = "analytics-group")
  public void handleCreated(EventEnvelope<TicketCreatedEvent> envelope) {
    analyticsService.processTicketCreated(envelope.payload(), envelope.occurredAt());
  }

  @KafkaListener(topics = "TICKET_UPDATED", groupId = "analytics-group")
  public void handleUpdated(EventEnvelope<TicketUpdatedEvent> envelope) {
    analyticsService.processTicketUpdated(envelope.payload(), envelope.occurredAt());
  }

  @KafkaListener(topics = "TICKET_RESOLVED", groupId = "analytics-group")
  public void handleResolved(EventEnvelope<TicketResolvedEvent> envelope) {
    analyticsService.processTicketResolved(envelope.payload(), envelope.occurredAt());
  }
}
