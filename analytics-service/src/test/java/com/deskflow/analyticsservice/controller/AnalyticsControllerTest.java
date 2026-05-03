package com.deskflow.analyticsservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deskflow.analyticsservice.dto.response.*;
import com.deskflow.analyticsservice.service.AnalyticsService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AnalyticsService analyticsService;

  @Test
  @DisplayName("GET /analytics/summary → 200 with summary response")
  void getSummary_returnsOk() throws Exception {
    SummaryResponse response = new SummaryResponse(100L, 60L, 5L, 3600000.0, 0.1);

    when(analyticsService.getSummary()).thenReturn(response);

    mockMvc
        .perform(get("/analytics/summary"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalTickets").value(100))
        .andExpect(jsonPath("$.openTickets").value(60))
        .andExpect(jsonPath("$.resolvedToday").value(5))
        .andExpect(jsonPath("$.avgResolutionTimeMs").value(3600000.0))
        .andExpect(jsonPath("$.slaBreachRate").value(0.1));
  }

  @Test
  @DisplayName("GET /analytics/by-status → 200 with grouped counts")
  void getByStatus_returnsOk() throws Exception {
    List<GroupedCountResponse> response =
        List.of(new GroupedCountResponse("OPEN", 10L), new GroupedCountResponse("IN_PROGRESS", 5L));

    when(analyticsService.getByStatus()).thenReturn(response);

    mockMvc
        .perform(get("/analytics/by-status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].label").value("OPEN"))
        .andExpect(jsonPath("$[0].count").value(10))
        .andExpect(jsonPath("$[1].label").value("IN_PROGRESS"))
        .andExpect(jsonPath("$[1].count").value(5));
  }

  @Test
  @DisplayName("GET /analytics/by-priority → 200 with grouped counts")
  void getByPriority_returnsOk() throws Exception {
    List<GroupedCountResponse> response =
        List.of(new GroupedCountResponse("HIGH", 8L), new GroupedCountResponse("LOW", 3L));

    when(analyticsService.getByPriority()).thenReturn(response);

    mockMvc
        .perform(get("/analytics/by-priority"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].label").value("HIGH"))
        .andExpect(jsonPath("$[0].count").value(8));
  }

  @Test
  @DisplayName("GET /analytics/by-category → 200 with grouped counts")
  void getByCategory_returnsOk() throws Exception {
    List<GroupedCountResponse> response =
        List.of(new GroupedCountResponse("HARDWARE", 12L), new GroupedCountResponse("NETWORK", 4L));

    when(analyticsService.getByCategory()).thenReturn(response);

    mockMvc
        .perform(get("/analytics/by-category"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].label").value("HARDWARE"))
        .andExpect(jsonPath("$[0].count").value(12));
  }

  @Test
  @DisplayName("GET /analytics/by-agent → 200 with agent stats")
  void getByAgent_returnsOk() throws Exception {
    List<AgentStatsResponse> response =
        List.of(
            new AgentStatsResponse("agent-1", 10L, 7L, 3600000.0),
            new AgentStatsResponse("agent-2", 5L, 5L, 1800000.0));

    when(analyticsService.getByAgent()).thenReturn(response);

    mockMvc
        .perform(get("/analytics/by-agent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].assigneeId").value("agent-1"))
        .andExpect(jsonPath("$[0].ticketsAssigned").value(10))
        .andExpect(jsonPath("$[0].ticketsResolved").value(7))
        .andExpect(jsonPath("$[0].avgResolutionTimeMs").value(3600000.0));
  }

  @Test
  @DisplayName("GET /analytics/sla-breach-rate → 200 with breach stats")
  void getSlaBreachRate_noParams_returnsOk() throws Exception {
    SlaBreachResponse response = new SlaBreachResponse(100L, 15L, 0.15);

    when(analyticsService.getSlaBreachRate(null, null)).thenReturn(response);

    mockMvc
        .perform(get("/analytics/sla-breach-rate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalTickets").value(100))
        .andExpect(jsonPath("$.breachedTickets").value(15))
        .andExpect(jsonPath("$.breachRate").value(0.15));
  }

  @Test
  @DisplayName("GET /analytics/sla-breach-rate → 200 with from and to params")
  void getSlaBreachRate_withParams_returnsOk() throws Exception {
    Instant from = Instant.parse("2025-01-01T00:00:00Z");
    Instant to = Instant.parse("2025-01-31T23:59:59Z");
    SlaBreachResponse response = new SlaBreachResponse(50L, 5L, 0.1);

    when(analyticsService.getSlaBreachRate(from, to)).thenReturn(response);

    mockMvc
        .perform(
            get("/analytics/sla-breach-rate")
                .param("from", from.toString())
                .param("to", to.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalTickets").value(50))
        .andExpect(jsonPath("$.breachedTickets").value(5));
  }

  @Test
  @DisplayName("GET /analytics/resolution-trend → 200 with daily counts")
  void getResolutionTrend_noParams_returnsOk() throws Exception {
    List<ResolutionTrendResponse> response =
        List.of(
            new ResolutionTrendResponse(LocalDate.of(2025, 1, 1), 5L),
            new ResolutionTrendResponse(LocalDate.of(2025, 1, 2), 8L));

    when(analyticsService.getResolutionTrend(null, null)).thenReturn(response);

    mockMvc
        .perform(get("/analytics/resolution-trend"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].date").value("2025-01-01"))
        .andExpect(jsonPath("$[0].resolvedCount").value(5))
        .andExpect(jsonPath("$[1].date").value("2025-01-02"))
        .andExpect(jsonPath("$[1].resolvedCount").value(8));
  }

  @Test
  @DisplayName("GET /analytics/resolution-trend → 200 with from and to params")
  void getResolutionTrend_withParams_returnsOk() throws Exception {
    Instant from = Instant.parse("2025-01-01T00:00:00Z");
    Instant to = Instant.parse("2025-01-31T23:59:59Z");
    List<ResolutionTrendResponse> response =
        List.of(new ResolutionTrendResponse(LocalDate.of(2025, 1, 15), 3L));

    when(analyticsService.getResolutionTrend(from, to)).thenReturn(response);

    mockMvc
        .perform(
            get("/analytics/resolution-trend")
                .param("from", from.toString())
                .param("to", to.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].resolvedCount").value(3));
  }
}
