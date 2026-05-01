package com.deskflow.notificationservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PagedNotificationRequest(
    Boolean isRead, @Min(0) Integer page, @Min(1) @Max(100) Integer size) {

  public PagedNotificationRequest {
    if (page == null) page = 0;
    if (size == null) size = 20;
  }
}
