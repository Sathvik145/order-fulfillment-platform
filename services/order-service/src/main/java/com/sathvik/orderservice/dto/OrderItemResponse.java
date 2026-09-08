package com.sathvik.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        String productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}