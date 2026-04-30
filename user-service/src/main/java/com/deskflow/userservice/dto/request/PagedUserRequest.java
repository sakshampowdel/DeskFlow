package com.deskflow.userservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PagedUserRequest(
    String role, String department, @Min(0) Integer page, @Min(1) @Max(100) Integer size) {

  public PagedUserRequest {
    if (page == null) page = 0;
    if (size == null) size = 20;
  }
}
