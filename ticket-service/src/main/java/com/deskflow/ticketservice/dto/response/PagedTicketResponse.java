package com.deskflow.ticketservice.dto.response;

import java.util.List;

public record PagedTicketResponse(
    List<TicketSummaryResponse> tickets,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last) {}
