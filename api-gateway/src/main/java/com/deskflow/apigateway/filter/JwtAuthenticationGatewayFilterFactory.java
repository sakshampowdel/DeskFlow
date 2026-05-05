package com.deskflow.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationGatewayFilterFactory
    extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

  @Value("${jwt.secret}")
  private String secret;

  public JwtAuthenticationGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();

      if (!request.getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
        return onError(exchange, "No Authorization Header", HttpStatus.UNAUTHORIZED);
      }

      String authHeader = request.getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).get(0);
      if (!authHeader.startsWith("Bearer ")) {
        return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
      }

      String token = authHeader.substring(7);

      try {
        Claims claims =
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

        String userRole = String.valueOf(claims.get("role"));

        if (config.getRequiredRoles() != null && !config.getRequiredRoles().contains(userRole)) {
          return onError(exchange, "Access Denied", HttpStatus.FORBIDDEN);
        }

        ServerHttpRequest modifiedRequest =
            exchange
                .getRequest()
                .mutate()
                .headers(
                    httpHeaders -> {
                      httpHeaders.remove("X-User-Id");
                      httpHeaders.remove("X-User-Role");
                      httpHeaders.remove(HttpHeaders.AUTHORIZATION);
                    })
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Role", userRole)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());

      } catch (Exception e) {
        return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
      }
    };
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = Base64.getDecoder().decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
    exchange.getResponse().setStatusCode(status);
    return exchange.getResponse().setComplete();
  }

  @Data
  public static class Config {
    private List<String> requiredRoles;
  }
}
