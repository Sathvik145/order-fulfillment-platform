package com.sathvik.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StockRequest(

        @NotBlank
        String productId,

        @Min(0)
        int quantity

) {
}