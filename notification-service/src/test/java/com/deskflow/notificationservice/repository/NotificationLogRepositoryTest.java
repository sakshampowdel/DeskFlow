package com.deskflow.notificationservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deskflow.notificationservice.config.MongoConfig;
import com.deskflow.notificationservice.model.KafkaEventType;
import com.deskflow.notificationservice.model.NotificationLog;
import com.deskflow.notificationservice.model.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataMongoTest
@Import(MongoConfig.class)
class NotificationLogRepositoryTest {

  @Autowired private NotificationLogRepository repository;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
  }

  @Test
  @DisplayName("save → Should persist notification with audit dates")
  void save_PersistsNotification() {
    NotificationLog log =
        new NotificationLog(
            "user-1",
            Type.EMAIL,
            KafkaEventType.TICKET_CREATED,
            "New Ticket",
            "A ticket has been created",
            "T-100");

    NotificationLog saved = repository.save(log);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getSentAt()).isNotNull(); // Verified by @CreatedDate
    assertThat(saved.isRead()).isFalse(); // Default state
  }

  @Test
  @DisplayName("findByRecipientId → Should return paged results for specific user")
  void findByRecipientId_ReturnsPagedData() {
    repository.save(
        new NotificationLog("user-1", Type.EMAIL, KafkaEventType.TICKET_CREATED, "T1", "B", "T-1"));
    repository.save(
        new NotificationLog("user-2", Type.EMAIL, KafkaEventType.TICKET_CREATED, "T2", "B", "T-2"));

    Page<NotificationLog> page = repository.findByRecipientId("user-1", PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isEqualTo(1);
    assertThat(page.getContent().get(0).getRecipientId()).isEqualTo("user-1");
  }

  @Test
  @DisplayName("findByRecipientIdAndIsRead → Should filter by read status")
  void findByRecipientIdAndIsRead_FiltersCorrectly() {
    String userId = "user-1";
    NotificationLog unread =
        new NotificationLog(
            userId, Type.WEBSOCKET_PUSH, KafkaEventType.TICKET_UPDATED, "Unread", "B", "T-1");
    unread.setRead(false);

    NotificationLog read =
        new NotificationLog(
            userId, Type.WEBSOCKET_PUSH, KafkaEventType.TICKET_UPDATED, "Read", "B", "T-1");
    read.setRead(true);

    repository.save(unread);
    repository.save(read);

    Page<NotificationLog> unreadPage =
        repository.findByRecipientIdAndIsRead(userId, false, PageRequest.of(0, 10));

    assertThat(unreadPage.getTotalElements()).isEqualTo(1);
    assertThat(unreadPage.getContent().get(0).getTitle()).isEqualTo("Unread");
  }

  @Test
  @DisplayName("countByRecipientIdAndIsRead → Should return accurate unread count")
  void countByRecipientIdAndIsRead_ReturnsCount() {
    String userId = "user-1";
    NotificationLog n1 =
        new NotificationLog(userId, Type.EMAIL, KafkaEventType.TICKET_RESOLVED, "T", "B", "T-1");
    n1.setRead(false);
    NotificationLog n2 =
        new NotificationLog(userId, Type.EMAIL, KafkaEventType.TICKET_RESOLVED, "T", "B", "T-1");
    n2.setRead(false);
    NotificationLog n3 =
        new NotificationLog(userId, Type.EMAIL, KafkaEventType.TICKET_RESOLVED, "T", "B", "T-1");
    n3.setRead(true);

    repository.save(n1);
    repository.save(n2);
    repository.save(n3);

    long count = repository.countByRecipientIdAndIsRead(userId, false);

    assertThat(count).isEqualTo(2);
  }
}
