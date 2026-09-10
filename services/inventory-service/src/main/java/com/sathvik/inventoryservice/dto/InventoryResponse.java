package com.sathvik.inventoryservice.dto;

import java.time.Instant;

public record InventoryResponse(

        String productId,
        int availableQuantity,
        int reservedQuantity,
        Instant updatedAt

) {
}