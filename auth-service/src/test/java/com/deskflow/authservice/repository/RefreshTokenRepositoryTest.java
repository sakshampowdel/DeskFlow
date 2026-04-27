package com.deskflow.authservice.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.deskflow.authservice.config.MongoConfig;
import com.deskflow.authservice.model.RefreshToken;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;

@DataMongoTest
@Import(MongoConfig.class)
public class RefreshTokenRepositoryTest {

  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @BeforeEach
  void setUp() {
    refreshTokenRepository.deleteAll();
  }

  @Test
  @DisplayName("Save token and populate system-generated fields")
  void save_persistsTokenAndPopulatesAuditFields() {
    RefreshToken token =
        new RefreshToken("test-token", "user123", Instant.now().plus(1, ChronoUnit.DAYS));

    RefreshToken saved = refreshTokenRepository.save(token);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getToken()).isEqualTo("test-token");
    assertThat(saved.isRevoked()).isFalse();
  }

  @Test
  @DisplayName("Find by token returns the correct entity")
  void findByToken_whenTokenExists_returnsToken() {
    String tokenValue = "secret-refresh-token";
    RefreshToken token =
        new RefreshToken(tokenValue, "user456", Instant.now().plus(7, ChronoUnit.DAYS));
    refreshTokenRepository.save(token);

    Optional<RefreshToken> result = refreshTokenRepository.findByToken(tokenValue);

    assertThat(result).isPresent();
    assertThat(result.get().getUserId()).isEqualTo("user456");
    assertThat(result.get().getToken()).isEqualTo(tokenValue);
  }

  @Test
  @DisplayName("Find by token returns empty when token does not exist")
  void findByToken_whenTokenDoesNotExist_returnsEmpty() {
    Optional<RefreshToken> result = refreshTokenRepository.findByToken("non-existent-token");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Delete by User ID removes all tokens associated with that user")
  void deleteByUserId_removesSpecificUserTokens() {
    String userId = "target-user";
    RefreshToken token1 =
        new RefreshToken("token-1", userId, Instant.now().plus(1, ChronoUnit.HOURS));
    RefreshToken token2 =
        new RefreshToken("token-2", userId, Instant.now().plus(1, ChronoUnit.HOURS));
    RefreshToken token3 =
        new RefreshToken("token-3", "other-user", Instant.now().plus(1, ChronoUnit.HOURS));

    refreshTokenRepository.save(token1);
    refreshTokenRepository.save(token2);
    refreshTokenRepository.save(token3);

    // Act
    refreshTokenRepository.deleteByUserId(userId);

    // Assert
    assertThat(refreshTokenRepository.findByToken("token-1")).isEmpty();
    assertThat(refreshTokenRepository.findByToken("token-2")).isEmpty();
    assertThat(refreshTokenRepository.findByToken("token-3")).isPresent();
  }

  @Test
  @DisplayName("Update revoked status persists correctly")
  void save_afterUpdatingRevoked_updatesInDatabase() {
    RefreshToken token =
        new RefreshToken("active-token", "user789", Instant.now().plus(1, ChronoUnit.DAYS));
    RefreshToken saved = refreshTokenRepository.save(token);

    saved.setRevoked(true);
    refreshTokenRepository.save(saved);

    Optional<RefreshToken> updated = refreshTokenRepository.findByToken("active-token");
    assertThat(updated).isPresent();
    assertThat(updated.get().isRevoked()).isTrue();
  }
}
