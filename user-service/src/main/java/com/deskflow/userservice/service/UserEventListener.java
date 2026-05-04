package com.deskflow.userservice.service;

import com.deskflow.userservice.dto.event.EventEnvelope;
import com.deskflow.userservice.dto.event.UserRegisteredEvent;
import com.deskflow.userservice.dto.event.UserRoleChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {

  private final UserProfileService userProfileService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "USER_REGISTERED", groupId = "user-group")
  public void handleUserRegistered(EventEnvelope<?> envelope) {
    UserRegisteredEvent payload =
        objectMapper.convertValue(envelope.payload(), UserRegisteredEvent.class);
    userProfileService.createInitialProfile(payload.userId(), payload.email(), payload.role());
  }

  @KafkaListener(topics = "USER_ROLE_CHANGED", groupId = "user-group")
  public void handleRoleChanged(EventEnvelope<?> envelope) {
    UserRoleChangedEvent payload =
        objectMapper.convertValue(envelope.payload(), UserRoleChangedEvent.class);
    userProfileService.updateUserRole(payload.userId(), payload.newRole());
  }
}
