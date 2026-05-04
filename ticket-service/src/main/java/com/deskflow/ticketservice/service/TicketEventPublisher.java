package com.deskflow.ticketservice.service;

import com.deskflow.ticketservice.dto.event.EventEnvelope;
import com.deskflow.ticketservice.model.KafkaEventType;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void publish(KafkaEventType topic, Object payload) {
    var envelope = new EventEnvelope<>(UUID.randomUUID().toString(), topic, Instant.now(), payload);
    kafkaTemplate.send(String.valueOf(topic), envelope);
  }
}
