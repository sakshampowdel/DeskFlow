package com.deskflow.analyticsservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deskflow.analyticsservice.dto.response.GroupedCountResponse;
import com.deskflow.analyticsservice.model.*;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

@DataMongoTest
class TicketEventRepositoryTest {

  @Autowired private TicketEventRepository ticketEventRepository;

  @BeforeEach
  void setUp() {
    ticketEventRepository.deleteAll();
  }

  @Test
  @DisplayName("Save ticket event and verify persisted fields")
  void save_persistsTicketEvent() {
    TicketEvent event =
        new TicketEvent(
            "ticket-1",
            KafkaEventType.TICKET_CREATED,
            null,
            Priority.HIGH,
            Category.HARDWARE,
            "reporter-1",
            null,
            Instant.now());

    TicketEvent saved = ticketEventRepository.save(event);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getTicketId()).isEqualTo("ticket-1");
    assertThat(saved.getEventType()).isEqualTo(KafkaEventType.TICKET_CREATED);
    assertThat(saved.getPriority()).isEqualTo(Priority.HIGH);
  }

  @Test
  @DisplayName("countGroupedByStatus returns correct counts per status")
  void countGroupedByStatus_returnsCorrectCounts() {
    ticketEventRepository.save(
        new TicketEvent(
            "ticket-1",
            KafkaEventType.TICKET_UPDATED,
            Status.OPEN,
            Priority.LOW,
            Category.OTHER,
            "reporter-1",
            null,
            Instant.now()));
    ticketEventRepository.save(
        new TicketEvent(
            "ticket-2",
            KafkaEventType.TICKET_UPDATED,
            Status.OPEN,
            Priority.LOW,
            Category.OTHER,
            "reporter-2",
            null,
            Instant.now()));
    ticketEventRepository.save(
        new TicketEvent(
            "ticket-3",
            KafkaEventType.TICKET_UPDATED,
            Status.IN_PROGRESS,
            Priority.HIGH,
            Category.HARDWARE,
            "reporter-3",
            null,
            Instant.now()));

    List<GroupedCountResponse> result = ticketEventRepository.countGroupedByStatus();

    assertThat(result).isNotEmpty();
    GroupedCountResponse openGroup =
        result.stream().filter(r -> r.label().equals(Status.OPEN.name())).findFirst().orElseThrow();
    assertThat(openGroup.count()).isEqualTo(2);
  }

  @Test
  @DisplayName("countGroupedByStatus ignores events with null status")
  void countGroupedByStatus_ignoresNullStatus() {
    ticketEventRepository.save(
        new TicketEvent(
            "ticket-1",
            KafkaEventType.TICKET_CREATED,
            null,
            Priority.LOW,
            Category.OTHER,
            "reporter-1",
            null,
            Instant.now()));

    List<GroupedCountResponse> result = ticketEventRepository.countGroupedByStatus();

    assertThat(result).isEmpty();
  }
}
