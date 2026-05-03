package com.deskflow.analyticsservice.dto.response;

public record SlaBreachResponse(long totalTickets, long breachedTickets, double breachRate) {}
