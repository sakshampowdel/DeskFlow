package com.deskflow.notificationservice.dto.event;

import com.deskflow.notificationservice.model.KafkaEventType;
import java.time.Instant;

public record EventEnvelope<T>(
    String eventId, KafkaEventType eventType, Instant occurredAt, T payload) {}
