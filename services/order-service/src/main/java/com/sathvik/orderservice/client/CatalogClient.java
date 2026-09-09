package com.sathvik.orderservice.client;

import com.sathvik.orderservice.dto.CatalogProductResponse;
import com.sathvik.orderservice.exception.CatalogServiceUnavailableException;
import com.sathvik.orderservice.exception.ProductNotFoundException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class CatalogClient {

    private static final Logger log =
            LoggerFactory.getLogger(CatalogClient.class);

    private final RestClient restClient;
    private final Retry catalogRetry;
    private final CircuitBreaker catalogCircuitBreaker;

    public CatalogClient(
            RestClient.Builder restClientBuilder,
            Retry catalogRetry,
            CircuitBreaker catalogCircuitBreaker
    ) {

        this.restClient = restClientBuilder
                .baseUrl("http://localhost:8083")
                .build();

        this.catalogRetry = catalogRetry;
        this.catalogCircuitBreaker = catalogCircuitBreaker;

        this.catalogRetry.getEventPublisher()
                .onRetry(event ->
                        log.warn(
                                "Retrying Catalog Service. Retry attempt: {}",
                                event.getNumberOfRetryAttempts()
                        )
                );

        this.catalogCircuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn(
                                "Catalog Circuit Breaker state changed: {}",
                                event.getStateTransition()
                        )
                );
    }

    public CatalogProductResponse getProduct(String productId) {

        try {

            return catalogCircuitBreaker.executeSupplier(
                    () -> catalogRetry.executeSupplier(
                            () -> fetchProduct(productId)
                    )
            );

        } catch (CallNotPermittedException ex) {

            log.warn(
                    "Catalog Service call blocked because Circuit Breaker is OPEN"
            );

            throw new CatalogServiceUnavailableException();
        }
    }

    private CatalogProductResponse fetchProduct(String productId) {

        log.info(
                "Calling Catalog Service for product: {}",
                productId
        );

        try {

            return restClient
                    .get()
                    .uri("/api/products/{productId}", productId)
                    .retrieve()
                    .body(CatalogProductResponse.class);

        } catch (HttpClientErrorException.NotFound ex) {

            throw new ProductNotFoundException(productId);

        } catch (ResourceAccessException ex) {

            throw new CatalogServiceUnavailableException();
        }
    }
}