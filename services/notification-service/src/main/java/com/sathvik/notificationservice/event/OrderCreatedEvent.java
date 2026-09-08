package com.sathvik.notificationservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(

        String eventId,
        String orderId,
        String userId,
        List<OrderItemEvent> items,
        BigDecimal totalAmount,
        String status,
        Instant createdAt

) {
}