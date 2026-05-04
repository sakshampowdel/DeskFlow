package com.deskflow.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // 1. Disable CSRF - Required for Postman/APIs to work
        .csrf(csrf -> csrf.disable())

        // 2. Configure which paths are public and which are private
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/register", "/auth/login")
                    .permitAll()
                    .anyRequest()
                    .authenticated())

        // 3. Make the session Stateless (Standard for JWT/Microservices)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
