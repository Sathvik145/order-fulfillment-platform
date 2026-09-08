package com.sathvik.catalogservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(

        String productId,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        Instant createdAt,
        Instant updatedAt

) {
}