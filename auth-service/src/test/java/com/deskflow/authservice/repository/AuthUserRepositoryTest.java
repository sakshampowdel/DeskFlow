package com.deskflow.authservice.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.deskflow.authservice.model.AuthUser;
import com.deskflow.authservice.model.Role;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;

@DataMongoTest
public class AuthUserRepositoryTest {

  @Autowired private AuthUserRepository authUserRepository;

  @BeforeEach
  void setUp() {
    authUserRepository.deleteAll();
  }

  @Test
  @DisplayName("Save user and populate system-generated fields")
  void save_persistsUserAndPopulatesAuditFields() {
    AuthUser user = new AuthUser("user@example.com", "hashedpassword123", Role.SUBMITTER);

    AuthUser saved = authUserRepository.save(user);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
    assertThat(saved.getEmail()).isEqualTo(user.getEmail());
  }

  @Test
  @DisplayName("Find user by email returns the correct user")
  void findByEmail_whenUserExists_returnsUser() {
    AuthUser user = new AuthUser("find@example.com", "hashedpassword123", Role.SUBMITTER);

    authUserRepository.save(user);

    Optional<AuthUser> result = authUserRepository.findByEmail(user.getEmail());

    assertThat(result).isPresent().get().hasFieldOrPropertyWithValue("email", user.getEmail());
  }

  @Test
  @DisplayName("Find by email returns empty when user does not exist")
  void findByEmail_whenUserDoesNotExist_returnsEmpty() {
    Optional<AuthUser> result = authUserRepository.findByEmail("ghost@example.com");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Duplicate email throws exception due to unique index")
  void save_duplicateEmail_throwsException() {
    authUserRepository.save(new AuthUser("dup@example.com", "hashedpassword123", Role.SUBMITTER));

    AuthUser duplicate = new AuthUser("dup@example.com", "hashedpassword123", Role.SUBMITTER);

    assertThrows(DuplicateKeyException.class, () -> authUserRepository.save(duplicate));
  }
}
