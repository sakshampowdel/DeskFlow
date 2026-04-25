package com.deskflow.authservice.model;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "auth_users")
@Getter
@NoArgsConstructor
public class AuthUser {
  @Id private String id;

  @Indexed(unique = true)
  @Setter
  private String email;

  @Setter private String passwordHash;

  @Setter private Role role;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  public AuthUser(String email, String passwordHash, Role role) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.role = role;
  }
}
