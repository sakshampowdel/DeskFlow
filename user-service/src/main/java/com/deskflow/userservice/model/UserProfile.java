package com.deskflow.userservice.model;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_profiles")
@Getter
@NoArgsConstructor
public class UserProfile implements Persistable<String> {

  @Id private String id;

  @Indexed(unique = true)
  private String email;

  private Role role;
  @Setter private String fullName;
  @Setter private String department;
  @Setter private String avatarUrl;
  @Setter private boolean isActive = true;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  public UserProfile(String id, String email, Role role) {
    this.id = id;
    this.email = email;
    this.role = role;
  }

  public void syncRole(Role newRole) {
    this.role = newRole;
  }

  public void syncEmail(String newEmail) {
    this.email = newEmail;
  }

  @Override
  public boolean isNew() {
    return createdAt == null;
  }
}
