package com.sathvik.inventoryservice.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(
            String productId,
            int requested,
            int available
    ) {
        super(
                "Insufficient stock for product " + productId +
                        ". Requested: " + requested +
                        ", Available: " + available
        );
    }
}