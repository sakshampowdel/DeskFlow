package com.deskflow.analyticsservice.service;

import com.deskflow.analyticsservice.dto.event.TicketCreatedEvent;
import com.deskflow.analyticsservice.dto.event.TicketResolvedEvent;
import com.deskflow.analyticsservice.dto.event.TicketUpdatedEvent;
import com.deskflow.analyticsservice.dto.response.*;
import com.deskflow.analyticsservice.model.KafkaEventType;
import com.deskflow.analyticsservice.model.Status;
import com.deskflow.analyticsservice.model.TicketEvent;
import com.deskflow.analyticsservice.model.TicketMetric;
import com.deskflow.analyticsservice.repository.TicketEventRepository;
import com.deskflow.analyticsservice.repository.TicketMetricRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

  private final TicketEventRepository ticketEventRepository;
  private final TicketMetricRepository ticketMetricRepository;

  public void processTicketCreated(TicketCreatedEvent event, Instant occurredAt) {
    TicketEvent ticketEvent =
        new TicketEvent(
            event.ticketId(),
            KafkaEventType.TICKET_CREATED,
            null,
            event.priority(),
            event.category(),
            event.reporterId(),
            null,
            occurredAt);
    ticketEventRepository.save(ticketEvent);

    TicketMetric metric =
        new TicketMetric(event.ticketId(), event.priority(), event.category(), null, occurredAt);
    ticketMetricRepository.save(metric);
  }

  public void processTicketUpdated(TicketUpdatedEvent event, Instant occurredAt) {
    TicketEvent ticketEvent =
        new TicketEvent(
            event.ticketId(),
            KafkaEventType.TICKET_UPDATED,
            event.newStatus(),
            event.priority(),
            null,
            event.reporterId(),
            event.assigneeId(),
            occurredAt);
    ticketEventRepository.save(ticketEvent);

    ticketMetricRepository
        .findById(event.ticketId())
        .ifPresent(
            metric -> {
              metric.setAssigneeId(event.assigneeId());
              metric.setPriority(event.priority());
              ticketMetricRepository.save(metric);
            });
  }

  public void processTicketResolved(TicketResolvedEvent event, Instant occurredAt) {
    TicketEvent ticketEvent =
        new TicketEvent(
            event.ticketId(),
            KafkaEventType.TICKET_RESOLVED,
            Status.RESOLVED,
            null,
            null,
            event.reporterId(),
            event.assigneeId(),
            occurredAt);
    ticketEventRepository.save(ticketEvent);

    ticketMetricRepository
        .findById(event.ticketId())
        .ifPresent(
            metric -> {
              metric.setResolvedAt(event.resolvedAt());
              metric.setTimeToResolveMs(
                  event.resolvedAt().toEpochMilli() - metric.getCreatedAt().toEpochMilli());
              metric.setSlaBreached(event.slaBreached());
              metric.setAssigneeId(event.assigneeId());
              ticketMetricRepository.save(metric);
            });
  }

  public SummaryResponse getSummary() {
    long totalTickets = ticketMetricRepository.count();
    long openTickets = ticketMetricRepository.countByResolvedAtIsNull();
    long resolvedToday = ticketMetricRepository.countResolvedToday();
    Double avgResolutionTimeMs = ticketMetricRepository.avgResolutionTimeMs();
    long breachedCount = ticketMetricRepository.countBySlaBreachedTrue();
    long resolvedCount = totalTickets - openTickets;
    double slaBreachRate = resolvedCount > 0 ? (double) breachedCount / resolvedCount : 0.0;

    return new SummaryResponse(
        totalTickets,
        openTickets,
        resolvedToday,
        avgResolutionTimeMs != null ? avgResolutionTimeMs : 0.0,
        slaBreachRate);
  }

  public List<GroupedCountResponse> getByStatus() {
    return ticketEventRepository.countGroupedByStatus();
  }

  public List<GroupedCountResponse> getByPriority() {
    return ticketMetricRepository.countGroupedByPriority();
  }

  public List<GroupedCountResponse> getByCategory() {
    return ticketMetricRepository.countGroupedByCategory();
  }

  public List<AgentStatsResponse> getByAgent() {
    return ticketMetricRepository.getAgentStats();
  }

  public SlaBreachResponse getSlaBreachRate(Instant from, Instant to) {
    Instant effectiveFrom = from != null ? from : Instant.EPOCH;
    Instant effectiveTo = to != null ? to : Instant.now();

    long total = ticketMetricRepository.countByCreatedAtBetween(effectiveFrom, effectiveTo);
    long breached =
        ticketMetricRepository.countBySlaBreachedTrueAndCreatedAtBetween(
            effectiveFrom, effectiveTo);
    double rate = total > 0 ? (double) breached / total : 0.0;

    return new SlaBreachResponse(total, breached, rate);
  }

  public List<ResolutionTrendResponse> getResolutionTrend(Instant from, Instant to) {
    Instant effectiveFrom = from != null ? from : Instant.now().minusSeconds(60L * 60 * 24 * 30);
    Instant effectiveTo = to != null ? to : Instant.now();

    return ticketMetricRepository.findResolvedBetween(effectiveFrom, effectiveTo).stream()
        .filter(m -> m.getResolvedAt() != null)
        .collect(
            Collectors.groupingBy(
                metric -> metric.getResolvedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                Collectors.counting()))
        .entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .map(e -> new ResolutionTrendResponse(e.getKey(), e.getValue()))
        .toList();
  }
}
