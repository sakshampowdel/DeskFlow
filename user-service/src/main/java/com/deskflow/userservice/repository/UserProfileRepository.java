package com.deskflow.userservice.repository;

import com.deskflow.userservice.model.UserProfile;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

  Optional<UserProfile> findByEmail(String email);
}
