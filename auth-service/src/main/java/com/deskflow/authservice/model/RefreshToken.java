package com.deskflow.authservice.model;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "refresh_token")
@Getter
@NoArgsConstructor
public class RefreshToken {

  @Id private String id;

  private String token;
  private String userId;
  private Instant expiresAt;
  @Setter
  private boolean revoked;

  @CreatedDate private Instant createdAt;

  public RefreshToken(String token, String userId, Instant expiresAt) {
    this.token = token;
    this.userId = userId;
    this.expiresAt = expiresAt;
    this.revoked = false;
  }
}
