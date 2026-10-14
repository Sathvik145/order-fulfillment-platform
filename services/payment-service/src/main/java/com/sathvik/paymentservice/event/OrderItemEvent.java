package com.sathvik.paymentservice.event;

import java.math.BigDecimal;

public record OrderItemEvent(
        String productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}