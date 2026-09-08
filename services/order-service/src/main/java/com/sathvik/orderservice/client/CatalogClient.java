package com.sathvik.orderservice.client;

import com.sathvik.orderservice.dto.CatalogProductResponse;
import com.sathvik.orderservice.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class CatalogClient {

    private final RestClient restClient;

    public CatalogClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8083")
                .build();
    }

    public CatalogProductResponse getProduct(String productId) {

        try {

            return restClient
                    .get()
                    .uri("/api/products/{productId}", productId)
                    .retrieve()
                    .body(CatalogProductResponse.class);

        } catch (HttpClientErrorException.NotFound ex) {

            throw new ProductNotFoundException(productId);
        }
    }
}