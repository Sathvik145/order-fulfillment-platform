package com.sathvik.orderservice.event;

import java.time.Instant;
import java.util.UUID;

public record InventoryFailedEvent(
        UUID eventId,
        UUID orderId,
        String reason,
        Instant createdAt
) {
}