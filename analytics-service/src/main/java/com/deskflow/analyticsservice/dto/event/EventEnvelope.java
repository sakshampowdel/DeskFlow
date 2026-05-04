package com.deskflow.analyticsservice.dto.event;

import com.deskflow.analyticsservice.model.KafkaEventType;
import java.time.Instant;

public record EventEnvelope<T>(
    String eventId, KafkaEventType eventType, Instant occurredAt, T payload) {}
