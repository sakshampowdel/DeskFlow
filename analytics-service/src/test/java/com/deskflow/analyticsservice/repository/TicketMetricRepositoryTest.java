package com.deskflow.analyticsservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deskflow.analyticsservice.dto.response.AgentStatsResponse;
import com.deskflow.analyticsservice.dto.response.GroupedCountResponse;
import com.deskflow.analyticsservice.model.Category;
import com.deskflow.analyticsservice.model.Priority;
import com.deskflow.analyticsservice.model.TicketMetric;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

@DataMongoTest
class TicketMetricRepositoryTest {

  @Autowired private TicketMetricRepository ticketMetricRepository;

  @BeforeEach
  void setUp() {
    ticketMetricRepository.deleteAll();
  }

  @Test
  @DisplayName("Save ticket metric and verify persisted fields")
  void save_persistsTicketMetric() {
    TicketMetric metric =
        new TicketMetric("ticket-1", Priority.HIGH, Category.HARDWARE, "agent-1", Instant.now());

    TicketMetric saved = ticketMetricRepository.save(metric);

    assertThat(saved.getId()).isEqualTo("ticket-1");
    assertThat(saved.getTicketId()).isEqualTo("ticket-1");
    assertThat(saved.getResolvedAt()).isNull();
    assertThat(saved.getTimeToResolveMs()).isNull();
  }

  @Test
  @DisplayName("countByResolvedAtIsNull returns only unresolved tickets")
  void countByResolvedAtIsNull_returnsUnresolvedOnly() {
    TicketMetric unresolved =
        new TicketMetric("ticket-1", Priority.LOW, Category.OTHER, null, Instant.now());

    TicketMetric resolved =
        new TicketMetric("ticket-2", Priority.HIGH, Category.HARDWARE, "agent-1", Instant.now());
    resolved.setResolvedAt(Instant.now());

    ticketMetricRepository.save(unresolved);
    ticketMetricRepository.save(resolved);

    assertThat(ticketMetricRepository.countByResolvedAtIsNull()).isEqualTo(1);
  }

  @Test
  @DisplayName("countBySlaBreachedTrue returns only breached tickets")
  void countBySlaBreachedTrue_returnsBreachedOnly() {
    TicketMetric breached =
        new TicketMetric("ticket-1", Priority.URGENT, Category.NETWORK, "agent-1", Instant.now());
    breached.setSlaBreached(true);

    TicketMetric notBreached =
        new TicketMetric("ticket-2", Priority.LOW, Category.OTHER, null, Instant.now());
    notBreached.setSlaBreached(false);

    ticketMetricRepository.save(breached);
    ticketMetricRepository.save(notBreached);

    assertThat(ticketMetricRepository.countBySlaBreachedTrue()).isEqualTo(1);
  }

  @Test
  @DisplayName("countByCreatedAtBetween returns tickets within time window")
  void countByCreatedAtBetween_returnsCorrectCount() {
    Instant now = Instant.now();

    ticketMetricRepository.save(
        new TicketMetric("ticket-1", Priority.LOW, Category.OTHER, null, now.minusSeconds(3600)));
    ticketMetricRepository.save(
        new TicketMetric("ticket-2", Priority.LOW, Category.OTHER, null, now.minusSeconds(1800)));
    ticketMetricRepository.save(
        new TicketMetric("ticket-3", Priority.LOW, Category.OTHER, null, now.minusSeconds(100)));

    long count = ticketMetricRepository.countByCreatedAtBetween(now.minusSeconds(2000), now);

    assertThat(count).isEqualTo(2);
  }

  @Test
  @DisplayName("findResolvedBetween returns only tickets resolved in window")
  void findResolvedBetween_returnsCorrectTickets() {
    Instant now = Instant.now();

    TicketMetric inWindow =
        new TicketMetric(
            "ticket-1", Priority.HIGH, Category.HARDWARE, "agent-1", now.minusSeconds(7200));
    inWindow.setResolvedAt(now.minusSeconds(3600));

    TicketMetric outOfWindow =
        new TicketMetric("ticket-2", Priority.LOW, Category.OTHER, null, now.minusSeconds(90000));
    outOfWindow.setResolvedAt(now.minusSeconds(86400));

    ticketMetricRepository.save(inWindow);
    ticketMetricRepository.save(outOfWindow);

    List<TicketMetric> result =
        ticketMetricRepository.findResolvedBetween(now.minusSeconds(7200), now);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTicketId()).isEqualTo("ticket-1");
  }

  @Test
  @DisplayName("avgResolutionTimeMs returns correct average")
  void avgResolutionTimeMs_returnsCorrectAverage() {
    Instant now = Instant.now();
    TicketMetric m1 =
        new TicketMetric("t1", Priority.HIGH, Category.HARDWARE, "a1", now.minusSeconds(7200));
    m1.setResolvedAt(now.minusSeconds(3600));
    m1.setTimeToResolveMs(3600000L);

    TicketMetric m2 =
        new TicketMetric("t2", Priority.LOW, Category.OTHER, "a2", now.minusSeconds(10000));
    m2.setResolvedAt(now.minusSeconds(5000));
    m2.setTimeToResolveMs(5000000L);

    ticketMetricRepository.saveAll(List.of(m1, m2));

    Double avg = ticketMetricRepository.avgResolutionTimeMs();
    assertThat(avg).isEqualTo(4300000.0);
  }

  @Test
  @DisplayName("countGroupedByPriority returns correct counts")
  void countGroupedByPriority_returnsCorrectCounts() {
    ticketMetricRepository.save(
        new TicketMetric("ticket-1", Priority.HIGH, Category.HARDWARE, null, Instant.now()));
    ticketMetricRepository.save(
        new TicketMetric("ticket-2", Priority.HIGH, Category.NETWORK, null, Instant.now()));
    ticketMetricRepository.save(
        new TicketMetric("ticket-3", Priority.LOW, Category.OTHER, null, Instant.now()));

    List<GroupedCountResponse> result = ticketMetricRepository.countGroupedByPriority();

    GroupedCountResponse highGroup =
        result.stream()
            .filter(r -> r.label().equals(Priority.HIGH.name()))
            .findFirst()
            .orElseThrow();
    assertThat(highGroup.count()).isEqualTo(2);
  }

  @Test
  @DisplayName("getAgentStats returns correct per-agent breakdown")
  void getAgentStats_returnsCorrectStats() {
    Instant now = Instant.now();

    TicketMetric m1 =
        new TicketMetric(
            "t-1", Priority.HIGH, Category.HARDWARE, "agent-1", now.minusSeconds(7200));
    m1.setResolvedAt(now.minusSeconds(3600));
    m1.setTimeToResolveMs(3600000L);

    TicketMetric m2 =
        new TicketMetric("t-2", Priority.LOW, Category.OTHER, "agent-1", now.minusSeconds(5000));
    ticketMetricRepository.save(m1);
    ticketMetricRepository.save(m2);

    List<AgentStatsResponse> result = ticketMetricRepository.getAgentStats();

    AgentStatsResponse stats =
        result.stream().filter(r -> r.assigneeId().equals("agent-1")).findFirst().orElseThrow();

    assertThat(stats.ticketsAssigned()).isEqualTo(2L);
    assertThat(stats.ticketsResolved()).isEqualTo(1L);
    assertThat(stats.avgResolutionTimeMs()).isEqualTo(3600000.0);
  }
}
