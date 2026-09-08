package com.sathvik.orderservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CatalogProductResponse(

        String productId,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        Instant createdAt,
        Instant updatedAt

) {
}