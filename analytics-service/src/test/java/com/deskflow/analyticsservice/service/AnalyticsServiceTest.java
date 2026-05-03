package com.deskflow.analyticsservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.deskflow.analyticsservice.dto.event.TicketCreatedEvent;
import com.deskflow.analyticsservice.dto.event.TicketResolvedEvent;
import com.deskflow.analyticsservice.dto.event.TicketUpdatedEvent;
import com.deskflow.analyticsservice.dto.response.ResolutionTrendResponse;
import com.deskflow.analyticsservice.dto.response.SlaBreachResponse;
import com.deskflow.analyticsservice.dto.response.SummaryResponse;
import com.deskflow.analyticsservice.model.*;
import com.deskflow.analyticsservice.repository.TicketEventRepository;
import com.deskflow.analyticsservice.repository.TicketMetricRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

  @Mock private TicketEventRepository ticketEventRepository;
  @Mock private TicketMetricRepository ticketMetricRepository;
  @InjectMocks private AnalyticsService analyticsService;

  @Test
  @DisplayName("processTicketCreated saves event and creates metric")
  void processTicketCreated_savesEventAndMetric() {
    TicketCreatedEvent event =
        new TicketCreatedEvent(
            "ticket-1",
            "Broken Laptop",
            Priority.HIGH,
            Category.HARDWARE,
            "reporter-1",
            Instant.now());

    when(ticketEventRepository.save(any(TicketEvent.class))).thenAnswer(i -> i.getArguments()[0]);
    when(ticketMetricRepository.save(any(TicketMetric.class))).thenAnswer(i -> i.getArguments()[0]);

    analyticsService.processTicketCreated(event);

    verify(ticketEventRepository)
        .save(
            argThat(
                e ->
                    e.getTicketId().equals("ticket-1")
                        && e.getEventType() == KafkaEventType.TICKET_CREATED
                        && e.getPriority() == Priority.HIGH));

    verify(ticketMetricRepository)
        .save(
            argThat(
                m ->
                    m.getTicketId().equals("ticket-1")
                        && m.getPriority() == Priority.HIGH
                        && m.getCategory() == Category.HARDWARE));
  }

  @Test
  @DisplayName("processTicketUpdated saves event and updates assignee on metric")
  void processTicketUpdated_savesEventAndUpdatesAssignee() {
    TicketUpdatedEvent event =
        new TicketUpdatedEvent(
            "ticket-1", Status.OPEN, Status.IN_PROGRESS, Priority.HIGH, "reporter-1", "agent-1");

    TicketMetric existingMetric =
        new TicketMetric("ticket-1", Priority.HIGH, Category.HARDWARE, null, Instant.now());

    when(ticketEventRepository.save(any(TicketEvent.class))).thenAnswer(i -> i.getArguments()[0]);
    when(ticketMetricRepository.findById("ticket-1")).thenReturn(Optional.of(existingMetric));
    when(ticketMetricRepository.save(any(TicketMetric.class))).thenAnswer(i -> i.getArguments()[0]);

    analyticsService.processTicketUpdated(event);

    verify(ticketEventRepository)
        .save(
            argThat(
                e ->
                    e.getTicketId().equals("ticket-1")
                        && e.getEventType() == KafkaEventType.TICKET_UPDATED
                        && e.getStatus() == Status.IN_PROGRESS));

    verify(ticketMetricRepository).save(argThat(m -> m.getAssigneeId().equals("agent-1")));
  }

  @Test
  @DisplayName("processTicketUpdated does nothing to metric if ticket not found")
  void processTicketUpdated_metricNotFound_onlySavesEvent() {
    TicketUpdatedEvent event =
        new TicketUpdatedEvent(
            "ticket-1", Status.OPEN, Status.IN_PROGRESS, Priority.HIGH, "reporter-1", "agent-1");

    when(ticketEventRepository.save(any(TicketEvent.class))).thenAnswer(i -> i.getArguments()[0]);
    when(ticketMetricRepository.findById("ticket-1")).thenReturn(Optional.empty());

    analyticsService.processTicketUpdated(event);

    verify(ticketEventRepository).save(any());
    verify(ticketMetricRepository, never()).save(any());
  }

  @Test
  @DisplayName("processTicketResolved saves event and updates metric resolution fields")
  void processTicketResolved_savesEventAndUpdatesMetric() {
    Instant createdAt = Instant.now().minusSeconds(3600);
    Instant resolvedAt = Instant.now();

    TicketResolvedEvent event =
        new TicketResolvedEvent(
            "ticket-1", "Broken Laptop", "reporter-1", "agent-1", resolvedAt, "Fixed it", true);

    TicketMetric existingMetric =
        new TicketMetric("ticket-1", Priority.HIGH, Category.HARDWARE, null, createdAt);

    when(ticketEventRepository.save(any(TicketEvent.class))).thenAnswer(i -> i.getArguments()[0]);
    when(ticketMetricRepository.findById("ticket-1")).thenReturn(Optional.of(existingMetric));
    when(ticketMetricRepository.save(any(TicketMetric.class))).thenAnswer(i -> i.getArguments()[0]);

    analyticsService.processTicketResolved(event);

    verify(ticketEventRepository)
        .save(
            argThat(
                e ->
                    e.getTicketId().equals("ticket-1")
                        && e.getEventType() == KafkaEventType.TICKET_RESOLVED));

    verify(ticketMetricRepository)
        .save(
            argThat(
                m ->
                    m.getResolvedAt().equals(resolvedAt)
                        && m.getSlaBreached() == true
                        && m.getAssigneeId().equals("agent-1")
                        && m.getTimeToResolveMs()
                            == resolvedAt.toEpochMilli() - createdAt.toEpochMilli()));
  }

  @Test
  @DisplayName("processTicketResolved does nothing to metric if ticket not found")
  void processTicketResolved_metricNotFound_onlySavesEvent() {
    TicketResolvedEvent event =
        new TicketResolvedEvent(
            "ticket-1", "Broken Laptop", "reporter-1", "agent-1", Instant.now(), "Fixed it", false);

    when(ticketEventRepository.save(any(TicketEvent.class))).thenAnswer(i -> i.getArguments()[0]);
    when(ticketMetricRepository.findById("ticket-1")).thenReturn(Optional.empty());

    analyticsService.processTicketResolved(event);

    verify(ticketEventRepository).save(any());
    verify(ticketMetricRepository, never()).save(any());
  }

  @Test
  @DisplayName("getSummary returns correct aggregated values")
  void getSummary_returnsCorrectValues() {
    when(ticketMetricRepository.count()).thenReturn(10L);
    when(ticketMetricRepository.countByResolvedAtIsNull()).thenReturn(6L);
    when(ticketMetricRepository.countResolvedToday()).thenReturn(2L);
    when(ticketMetricRepository.avgResolutionTimeMs()).thenReturn(3600000.0);
    when(ticketMetricRepository.countBySlaBreachedTrue()).thenReturn(2L);

    SummaryResponse response = analyticsService.getSummary();

    assertThat(response.totalTickets()).isEqualTo(10L);
    assertThat(response.openTickets()).isEqualTo(6L);
    assertThat(response.resolvedToday()).isEqualTo(2L);
    assertThat(response.avgResolutionTimeMs()).isEqualTo(3600000.0);
    assertThat(response.slaBreachRate()).isEqualTo(0.5);
  }

  @Test
  @DisplayName("getSummary handles null avgResolutionTimeMs")
  void getSummary_nullAvgResolutionTime_defaultsToZero() {
    when(ticketMetricRepository.count()).thenReturn(5L);
    when(ticketMetricRepository.countByResolvedAtIsNull()).thenReturn(5L);
    when(ticketMetricRepository.countResolvedToday()).thenReturn(0L);
    when(ticketMetricRepository.avgResolutionTimeMs()).thenReturn(null);
    when(ticketMetricRepository.countBySlaBreachedTrue()).thenReturn(0L);

    SummaryResponse response = analyticsService.getSummary();

    assertThat(response.avgResolutionTimeMs()).isEqualTo(0.0);
    assertThat(response.slaBreachRate()).isEqualTo(0.0);
  }

  @Test
  @DisplayName("getSlaBreachRate defaults from and to when null")
  void getSlaBreachRate_nullParams_usesDefaults() {
    when(ticketMetricRepository.countByCreatedAtBetween(any(), any())).thenReturn(10L);
    when(ticketMetricRepository.countBySlaBreachedTrueAndCreatedAtBetween(any(), any()))
        .thenReturn(3L);

    SlaBreachResponse response = analyticsService.getSlaBreachRate(null, null);

    assertThat(response.totalTickets()).isEqualTo(10L);
    assertThat(response.breachedTickets()).isEqualTo(3L);
    assertThat(response.breachRate()).isEqualTo(0.3);
  }

  @Test
  @DisplayName("getSlaBreachRate returns zero rate when no tickets in window")
  void getSlaBreachRate_noTickets_returnsZeroRate() {
    when(ticketMetricRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
    when(ticketMetricRepository.countBySlaBreachedTrueAndCreatedAtBetween(any(), any()))
        .thenReturn(0L);

    SlaBreachResponse response = analyticsService.getSlaBreachRate(null, null);

    assertThat(response.breachRate()).isEqualTo(0.0);
  }

  @Test
  @DisplayName("getResolutionTrend groups resolved tickets by day correctly")
  void getResolutionTrend_groupsByDay() {
    Instant now = Instant.now();
    Instant yesterday = now.minusSeconds(60L * 60 * 24);

    TicketMetric m1 =
        new TicketMetric(
            "ticket-1", Priority.HIGH, Category.HARDWARE, "agent-1", yesterday.minusSeconds(3600));
    m1.setResolvedAt(yesterday);

    TicketMetric m2 =
        new TicketMetric(
            "ticket-2", Priority.LOW, Category.OTHER, "agent-2", yesterday.minusSeconds(7200));
    m2.setResolvedAt(yesterday);

    TicketMetric m3 =
        new TicketMetric(
            "ticket-3", Priority.MEDIUM, Category.NETWORK, "agent-1", now.minusSeconds(3600));
    m3.setResolvedAt(now);

    when(ticketMetricRepository.findResolvedBetween(any(), any())).thenReturn(List.of(m1, m2, m3));

    List<ResolutionTrendResponse> result = analyticsService.getResolutionTrend(null, null);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).resolvedCount()).isEqualTo(2);
    assertThat(result.get(1).resolvedCount()).isEqualTo(1);
  }
}
