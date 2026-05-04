package com.deskflow.authservice.dto.event;

import com.deskflow.authservice.model.KafkaEventType;
import java.time.Instant;

public record EventEnvelope<T>(
    String eventId, KafkaEventType eventType, Instant occurredAt, T payload) {}
