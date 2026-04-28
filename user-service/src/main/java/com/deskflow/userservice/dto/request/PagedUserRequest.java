package com.deskflow.userservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.RequestParam;

public record PagedUserRequest(
    @RequestParam(required = false) String role,
    @RequestParam(required = false) String department,
    @Min(0) @RequestParam(defaultValue = "0") int page,
    @Min(1) @Max(100) @RequestParam(defaultValue = "20") int size) {}
