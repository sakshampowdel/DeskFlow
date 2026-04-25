package com.deskflow.authservice.repository;

import com.deskflow.authservice.model.RefreshToken;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
  Optional<RefreshToken> findByToken(String token);

  void deleteByUserId(String userId);
}
