package com.sathvik.orderservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentCompletedEvent(
        UUID eventId,
        UUID orderId,
        UUID paymentId,
        String userId,
        BigDecimal amount,
        Instant createdAt
) {
}