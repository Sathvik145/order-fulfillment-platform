package com.sathvik.orderservice.config;

import com.sathvik.orderservice.exception.CatalogServiceUnavailableException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public Retry catalogRetry() {

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(CatalogServiceUnavailableException.class)
                .build();

        return Retry.of("catalog-service", retryConfig);
    }

    @Bean
    public CircuitBreaker catalogCircuitBreaker() {

        CircuitBreakerConfig circuitBreakerConfig =
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(5)
                        .minimumNumberOfCalls(5)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .permittedNumberOfCallsInHalfOpenState(2)
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                        .recordExceptions(
                                CatalogServiceUnavailableException.class
                        )
                        .build();

        return CircuitBreaker.of(
                "catalog-service",
                circuitBreakerConfig
        );
    }
}