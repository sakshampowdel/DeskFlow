package com.deskflow.analyticsservice.repository;

import com.deskflow.analyticsservice.dto.response.GroupedCountResponse;
import com.deskflow.analyticsservice.model.TicketEvent;
import java.util.List;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketEventRepository extends MongoRepository<TicketEvent, String> {

  @Aggregation(
      pipeline = {
        "{ $group: { _id: '$status', count: { $sum: 1 } } }",
        "{ $project: { label: '$_id', count: 1, _id: 0 } }"
      })
  List<GroupedCountResponse> countGroupedByStatus();
}
