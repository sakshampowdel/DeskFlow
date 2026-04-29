package com.deskflow.ticketservice.dto.request;

import com.deskflow.ticketservice.model.Category;
import com.deskflow.ticketservice.model.Priority;
import com.deskflow.ticketservice.model.Status;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PagedTicketRequest(
    Status status,
    Priority priority,
    Category category,
    String assigneeId,
    Boolean slaBreached,
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size) {

  public PagedTicketRequest {
    if (page == null) {
      page = 0;
    }
    if (size == null) {
      size = 20;
    }
  }
}
