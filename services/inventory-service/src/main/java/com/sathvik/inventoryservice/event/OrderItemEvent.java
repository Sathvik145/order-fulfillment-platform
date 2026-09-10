package com.sathvik.inventoryservice.event;

import java.math.BigDecimal;

public record OrderItemEvent(
        String productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}