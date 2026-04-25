package com.example.authservice.repository;

import com.example.authservice.model.AuthUser;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserRepository extends MongoRepository<AuthUser, String> {

  Optional<AuthUser> findByEmail(String email);

  boolean existsByEmail(String email);
}
