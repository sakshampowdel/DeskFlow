package com.deskflow.ticketservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deskflow.ticketservice.config.MongoConfig;
import com.deskflow.ticketservice.model.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataMongoTest
@Import(MongoConfig.class)
class TicketRepositoryTest {

  @Autowired private TicketRepository ticketRepository;

  @BeforeEach
  void setUp() {
    ticketRepository.deleteAll();
  }

  @Test
  @DisplayName("Save ticket and verify defaults and audit fields")
  void save_persistsTicketWithDefaults() {
    // Arrange
    Ticket ticket =
        new Ticket(
            "Internet Down",
            "WiFi is not working in Room 302",
            Priority.HIGH,
            Category.NETWORK,
            "user-123",
            Instant.now().plus(4, ChronoUnit.HOURS));

    // Act
    Ticket saved = ticketRepository.save(ticket);

    // Assert
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getStatus()).isEqualTo(Status.OPEN); // Default from constructor
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
    assertThat(saved.getComments()).isEmpty();
  }

  @Test
  @DisplayName("Find tickets by reporter ID with pagination")
  void findByReporterId_paginated_returnsCorrectSubset() {
    // Arrange: Create 3 tickets for reporter-A and 1 for reporter-B
    String reporterA = "reporter-A";
    Instant deadline = Instant.now().plus(1, ChronoUnit.DAYS);

    ticketRepository.save(
        new Ticket("T1", "Desc", Priority.LOW, Category.OTHER, reporterA, deadline));
    ticketRepository.save(
        new Ticket("T2", "Desc", Priority.LOW, Category.OTHER, reporterA, deadline));
    ticketRepository.save(
        new Ticket("T3", "Desc", Priority.LOW, Category.OTHER, reporterA, deadline));
    ticketRepository.save(
        new Ticket("T4", "Desc", Priority.LOW, Category.OTHER, "reporter-B", deadline));

    // Act: Request first page with size 2
    PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("title").ascending());
    Page<Ticket> result = ticketRepository.findByReporterId(reporterA, pageRequest);

    // Assert
    assertThat(result.getTotalElements()).isEqualTo(3); // Total found in DB
    assertThat(result.getContent()).hasSize(2); // Current page size
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("T1");
    assertThat(result.getTotalPages()).isEqualTo(2);
  }

  @Test
  @DisplayName("Add comment to ticket and persist")
  void save_withComments_persistsNestedObjects() {
    // Arrange
    Ticket ticket =
        new Ticket(
            "Hardware Issue",
            "Broken screen",
            Priority.MEDIUM,
            Category.HARDWARE,
            "user-1",
            Instant.now());
    ticket.getComments().add(new TicketComment("tech-support-1", "Parts ordered", true));

    // Act
    Ticket saved = ticketRepository.save(ticket);
    Ticket fetched = ticketRepository.findById(saved.getId()).orElseThrow();

    // Assert
    assertThat(fetched.getComments()).hasSize(1);
    assertThat(fetched.getComments().get(0).getBody()).isEqualTo("Parts ordered");
    assertThat(fetched.getComments().get(0).isInternal()).isTrue();
  }

  @Test
  @DisplayName("Update ticket status and SLA breach status")
  void update_statusAndSla_persistsChanges() {
    // Arrange
    Ticket ticket =
        ticketRepository.save(
            new Ticket(
                "Slow App",
                "UI is lagging",
                Priority.LOW,
                Category.SOFTWARE_ACCESS,
                "user-2",
                Instant.now()));

    // Act
    ticket.setStatus(Status.IN_PROGRESS);
    ticket.setSlaBreached(true);
    ticket.setAssigneeId("agent-42");
    ticketRepository.save(ticket);

    Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();

    // Assert
    assertThat(updated.getStatus()).isEqualTo(Status.IN_PROGRESS);
    assertThat(updated.isSlaBreached()).isTrue();
    assertThat(updated.getAssigneeId()).isEqualTo("agent-42");
  }
}
