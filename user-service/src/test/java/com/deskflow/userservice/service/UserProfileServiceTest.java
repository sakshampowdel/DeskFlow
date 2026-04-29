package com.deskflow.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.deskflow.userservice.dto.request.UpdateProfileRequest;
import com.deskflow.userservice.dto.response.InternalUserResponse;
import com.deskflow.userservice.dto.response.UserProfileResponse;
import com.deskflow.userservice.exception.UserNotFoundException;
import com.deskflow.userservice.model.Role;
import com.deskflow.userservice.model.UserProfile;
import com.deskflow.userservice.repository.UserProfileRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

  @Mock private UserProfileRepository userProfileRepository;

  @InjectMocks private UserProfileService userProfileService;

  @Test
  @DisplayName("Get profile should return response when user exists")
  void getMyProfile_userExists_returnsResponse() {
    // Arrange
    String userId = UUID.randomUUID().toString();
    UserProfile profile = new UserProfile(userId, "test@test.com", Role.SUBMITTER);
    when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

    // Act
    UserProfileResponse response = userProfileService.getMyProfile(userId);

    // Assert
    assertThat(response.id()).isEqualTo(userId);
    assertThat(response.email()).isEqualTo("test@test.com");
    verify(userProfileRepository).findById(userId);
  }

  @Test
  @DisplayName("Get profile should throw exception when user not found")
  void getMyProfile_userNotFound_throwsException() {
    String userId = "non-existent";
    when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userProfileService.getMyProfile(userId));
  }

  @Test
  @DisplayName("Update profile should only modify provided fields")
  void updateMyProfile_validRequest_updatesFields() {
    // Arrange
    String userId = UUID.randomUUID().toString();
    UserProfile existingProfile = new UserProfile(userId, "test@test.com", Role.SUBMITTER);
    existingProfile.setFullName("Old Name");

    UpdateProfileRequest request = new UpdateProfileRequest("New Name", "IT", "http://avatar.url");

    when(userProfileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));
    when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArguments()[0]);

    // Act
    UserProfileResponse response = userProfileService.updateMyProfile(userId, request);

    // Assert
    assertThat(response.fullName()).isEqualTo("New Name");
    assertThat(response.department()).isEqualTo("IT");
    assertThat(response.avatarUrl()).isEqualTo("http://avatar.url");
    verify(userProfileRepository).save(existingProfile);
  }

  @Test
  @DisplayName("Deactivate user should set isActive to false")
  void deactivateUser_userExists_updatesStatus() {
    // Arrange
    String userId = UUID.randomUUID().toString();
    UserProfile profile = new UserProfile(userId, "test@test.com", Role.SUBMITTER);
    when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

    // Act
    userProfileService.deactivateUser(userId);

    // Assert
    assertThat(profile.isActive()).isFalse(); // Verifies business logic
    verify(userProfileRepository).save(profile);
  }

  @Test
  @DisplayName("Internal summary should return only essential info")
  void getInternalUserSummary_userExists_returnsSummary() {
    // Arrange
    String userId = UUID.randomUUID().toString();
    UserProfile profile = new UserProfile(userId, "test@test.com", Role.SUBMITTER);
    profile.setFullName("Internal User");
    when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

    // Act
    InternalUserResponse response = userProfileService.getInternalUserSummary(userId);

    // Assert
    assertThat(response.fullName()).isEqualTo("Internal User");
    assertThat(response.email()).isEqualTo("test@test.com");
    // Ensure standard profile response fields are not present if this were JSON
  }
}
