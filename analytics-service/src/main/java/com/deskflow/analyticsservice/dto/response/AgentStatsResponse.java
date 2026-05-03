package com.deskflow.analyticsservice.dto.response;

public record AgentStatsResponse(
    String assigneeId, long ticketsAssigned, long ticketsResolved, double avgResolutionTimeMs) {}
