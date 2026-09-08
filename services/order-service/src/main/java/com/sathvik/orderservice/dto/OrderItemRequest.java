package com.sathvik.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(

        @NotBlank
        String productId,

        @Positive
        int quantity

) {
}