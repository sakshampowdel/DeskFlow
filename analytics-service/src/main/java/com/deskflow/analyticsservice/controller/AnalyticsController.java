package com.deskflow.analyticsservice.controller;

import com.deskflow.analyticsservice.dto.response.*;
import com.deskflow.analyticsservice.service.AnalyticsService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  @GetMapping("/summary")
  public ResponseEntity<SummaryResponse> getSummary() {
    return ResponseEntity.ok(analyticsService.getSummary());
  }

  @GetMapping("/by-status")
  public ResponseEntity<List<GroupedCountResponse>> getByStatus() {
    return ResponseEntity.ok(analyticsService.getByStatus());
  }

  @GetMapping("/by-priority")
  public ResponseEntity<List<GroupedCountResponse>> getByPriority() {
    return ResponseEntity.ok(analyticsService.getByPriority());
  }

  @GetMapping("/by-category")
  public ResponseEntity<List<GroupedCountResponse>> getByCategory() {
    return ResponseEntity.ok(analyticsService.getByCategory());
  }

  @GetMapping("/by-agent")
  public ResponseEntity<List<AgentStatsResponse>> getByAgent() {
    return ResponseEntity.ok(analyticsService.getByAgent());
  }

  @GetMapping("/sla-breach-rate")
  public ResponseEntity<SlaBreachResponse> getSlaBreachRate(
      @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to) {
    return ResponseEntity.ok(analyticsService.getSlaBreachRate(from, to));
  }

  @GetMapping("/resolution-trend")
  public ResponseEntity<List<ResolutionTrendResponse>> getResolutionTrend(
      @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to) {
    return ResponseEntity.ok(analyticsService.getResolutionTrend(from, to));
  }
}
