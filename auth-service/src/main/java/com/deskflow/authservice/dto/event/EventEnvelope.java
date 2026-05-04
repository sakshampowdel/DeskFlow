package com.deskflow.authservice.dto.event;

import com.deskflow.authservice.model.KafkaEventType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record EventEnvelope<T>(
    @JsonProperty("eventId") String eventId,
    @JsonProperty("eventType") KafkaEventType eventType,
    @JsonProperty("occurredAt") Instant occurredAt,
    @JsonProperty("payload") T payload) {}
