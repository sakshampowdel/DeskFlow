package com.deskflow.analyticsservice.repository;

import com.deskflow.analyticsservice.dto.response.AgentStatsResponse;
import com.deskflow.analyticsservice.dto.response.GroupedCountResponse;
import com.deskflow.analyticsservice.model.TicketMetric;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketMetricRepository extends MongoRepository<TicketMetric, String> {

  long countByResolvedAtIsNull();

  long countBySlaBreachedTrue();

  long countByCreatedAtBetween(Instant from, Instant to);

  long countBySlaBreachedTrueAndCreatedAtBetween(Instant from, Instant to);

  @Query("{ 'resolvedAt': { $gte: ?0, $lte: ?1 } }")
  List<TicketMetric> findResolvedBetween(Instant from, Instant to);

  @Aggregation(
      pipeline = {
        "{ $match: { resolvedAt: { $gte: { $dateFromParts: { "
            + "  year: { $year: '$$NOW' }, month: { $month: '$$NOW' }, day: { $dayOfMonth: '$$NOW' } "
            + "} } } } }",
        "{ $count: 'count' }"
      })
  long countResolvedToday();

  @Aggregation(
      pipeline = {
        "{ $match: { timeToResolveMs: { $ne: null } } }",
        "{ $group: { _id: null, avg: { $avg: '$timeToResolveMs' } } }",
        "{ $project: { _id: 0, avg: 1 } }"
      })
  Double avgResolutionTimeMs();

  @Aggregation(
      pipeline = {
        "{ $group: { _id: '$priority', count: { $sum: 1 } } }",
        "{ $project: { label: '$_id', count: 1, _id: 0 } }"
      })
  List<GroupedCountResponse> countGroupedByPriority();

  @Aggregation(
      pipeline = {
        "{ $group: { _id: '$category', count: { $sum: 1 } } }",
        "{ $project: { label: '$_id', count: 1, _id: 0 } }"
      })
  List<GroupedCountResponse> countGroupedByCategory();

  @Aggregation(
      pipeline = {
        "{ $match: { assigneeId: { $ne: null } } }",
        "{ $group: { "
            + "    _id: '$assigneeId', "
            + "    ticketsAssigned: { $sum: 1 }, "
            + "    ticketsResolved: { "
            + "      $sum: { "
            + "        $cond: [ "
            + "          { $gt: ['$resolvedAt', null] }, "
            + "          1, 0 "
            + "        ] "
            + "      } "
            + "    }, "
            + "    avgResTime: { $avg: '$timeToResolveMs' } "
            + "} }",
        "{ $project: { "
            + "    assigneeId: '$_id', "
            + "    ticketsAssigned: 1, "
            + "    ticketsResolved: 1, "
            + "    avgResolutionTimeMs: { $ifNull: ['$avgResTime', 0.0] }, "
            + "    _id: 0 "
            + "} }"
      })
  List<AgentStatsResponse> getAgentStats();
}
