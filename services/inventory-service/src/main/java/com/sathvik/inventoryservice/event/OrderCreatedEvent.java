package com.sathvik.inventoryservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        String userId,
        List<OrderItemEvent> items,
        BigDecimal totalAmount,
        String status,
        Instant createdAt
) {
}