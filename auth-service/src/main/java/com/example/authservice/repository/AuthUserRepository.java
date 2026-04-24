package com.example.authservice.repository;

import com.example.authservice.model.AuthUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuthUserRepository extends MongoRepository<AuthUser, String> {
}
