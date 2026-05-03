package com.deskflow.analyticsservice.dto.response;

public record SummaryResponse(
    long totalTickets,
    long openTickets,
    long resolvedToday,
    double avgResolutionTimeMs,
    double slaBreachRate) {}
