package com.deskflow.ticketservice.dto.event;

import com.deskflow.ticketservice.model.KafkaEventType;

import java.time.Instant;

public record EventEnvelope<T>(
    String eventId, KafkaEventType eventType, Instant occurredAt, T payload) {}
