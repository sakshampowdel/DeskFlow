package com.deskflow.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CustomLoggingGatewayFilterFactory
    extends AbstractGatewayFilterFactory<CustomLoggingGatewayFilterFactory.Config> {

  public CustomLoggingGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(CustomLoggingGatewayFilterFactory.Config config) {
    return ((exchange, chain) -> {
      log.info("API Gateway: Request to {}", exchange.getRequest().getPath());

      return chain
          .filter(exchange)
          .then(
              Mono.fromRunnable(
                  () -> {
                    log.info("API Gateway: Response code {}", exchange.getResponse());
                  }));
    });
  }

  public static class Config {}
}
