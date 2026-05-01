package com.deskflow.notificationservice.repository;

import com.deskflow.notificationservice.model.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
  Page<NotificationLog> findByRecipientId(String recipientId, Pageable pageable);

  Page<NotificationLog> findByRecipientIdAndIsRead(
      String recipientId, boolean isRead, Pageable pageable);

  long countByRecipientIdAndIsRead(String recipientId, boolean isRead);
}
