package com.deskflow.ticketservice.repository;

import com.deskflow.ticketservice.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository
    extends MongoRepository<Ticket, String>, QueryByExampleExecutor<Ticket> {
  Page<Ticket> findByReporterId(String reporterId, Pageable pageable);
}
