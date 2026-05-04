package com.deskflow.userservice.dto.event;

import com.deskflow.userservice.model.KafkaEventType;
import java.time.Instant;

public record EventEnvelope<T>(
    String eventId, KafkaEventType eventType, Instant occurredAt, T payload) {}
