package com.deskflow.userservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.deskflow.userservice.config.MongoConfig;
import com.deskflow.userservice.model.Role;
import com.deskflow.userservice.model.UserProfile;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;

@DataMongoTest
@Import(MongoConfig.class)
public class UserProfileRepositoryTest {

  @Autowired private UserProfileRepository userProfileRepository;

  @BeforeEach
  void setUp() {
    userProfileRepository.deleteAll();
  }

  @Test
  @DisplayName("Save user profile with manual ID and populate audit fields")
  void save_persistsProfileAndPopulatesAuditFields() {
    // IDs are synced from Auth service
    String userId = UUID.randomUUID().toString();
    UserProfile profile = new UserProfile(userId, "user@example.com", Role.SUBMITTER);
    profile.setFullName("John Doe");

    UserProfile saved = userProfileRepository.save(profile);

    assertThat(saved.getId()).isEqualTo(userId); // ID should match provided ID
    assertThat(saved.getEmail()).isEqualTo("user@example.com");
    assertThat(saved.isActive()).isTrue(); // Default value
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Find profile by email")
  void findByEmail_whenExists_returnsProfile() {
    String userId = UUID.randomUUID().toString();
    UserProfile profile = new UserProfile(userId, "find@example.com", Role.SUBMITTER);
    userProfileRepository.save(profile);

    Optional<UserProfile> result = userProfileRepository.findByEmail("find@example.com");

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(userId);
  }

  @Test
  @DisplayName("Duplicate email throws exception")
  void save_duplicateEmail_throwsException() {
    userProfileRepository.save(
        new UserProfile(UUID.randomUUID().toString(), "dup@example.com", Role.SUBMITTER));

    // Different ID, same email
    UserProfile duplicate =
        new UserProfile(UUID.randomUUID().toString(), "dup@example.com", Role.SUBMITTER);

    assertThrows(DuplicateKeyException.class, () -> userProfileRepository.save(duplicate));
  }

  @Test
  @DisplayName("Update profile fields correctly")
  void update_modifiesFieldsAndAuditTimestamp() throws InterruptedException {
    String userId = UUID.randomUUID().toString();
    UserProfile profile = new UserProfile(userId, "update@example.com", Role.SUBMITTER);
    UserProfile saved = userProfileRepository.save(profile);
    java.time.Instant firstUpdate = saved.getUpdatedAt();

    // Simulate update after brief delay
    Thread.sleep(10);
    saved.setFullName("New Name");
    saved.setDepartment("Engineering");
    UserProfile updated = userProfileRepository.save(saved);

    assertThat(updated.getFullName()).isEqualTo("New Name");
    assertThat(updated.getDepartment()).isEqualTo("Engineering");
    assertThat(updated.getUpdatedAt()).isAfter(firstUpdate); // Verifies @LastModifiedDate
  }
}
