package com.deskflow.analyticsservice.dto.response;

import java.time.LocalDate;

public record ResolutionTrendResponse(LocalDate date, long resolvedCount) {}
