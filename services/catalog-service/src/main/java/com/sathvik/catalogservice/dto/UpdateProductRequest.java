package com.sathvik.catalogservice.dto;

import java.math.BigDecimal;

public record UpdateProductRequest(
        String name,
        String description,
        BigDecimal price,
        Boolean active
) {
}