package com.deskflow.userservice.service;

import com.deskflow.userservice.dto.event.EventEnvelope;
import com.deskflow.userservice.dto.event.UserRegisteredEvent;
import com.deskflow.userservice.dto.event.UserRoleChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {

  private final UserProfileService userProfileService;

  @KafkaListener(topics = "USER_REGISTERED", groupId = "user-service-group")
  public void handleUserRegistered(EventEnvelope<UserRegisteredEvent> envelope) {
    UserRegisteredEvent payload = envelope.payload();
    userProfileService.createInitialProfile(payload.userId(), payload.email(), payload.role());
  }

  @KafkaListener(topics = "USER_ROLE_CHANGED", groupId = "user-service-group")
  public void handleRoleChanged(EventEnvelope<UserRoleChangedEvent> envelope) {
    UserRoleChangedEvent payload = envelope.payload();
    userProfileService.updateUserRole(payload.userId(), payload.newRole());
  }
}
