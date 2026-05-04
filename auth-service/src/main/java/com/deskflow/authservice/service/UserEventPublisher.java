package com.deskflow.authservice.service;

import com.deskflow.authservice.dto.event.EventEnvelope;
import com.deskflow.authservice.model.KafkaEventType;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void publish(KafkaEventType topic, Object payload) {
    var envelope = new EventEnvelope<>(UUID.randomUUID().toString(), topic, Instant.now(), payload);
    kafkaTemplate.send(String.valueOf(topic), envelope);
  }
}
