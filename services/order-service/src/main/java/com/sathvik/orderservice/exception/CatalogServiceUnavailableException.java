package com.sathvik.orderservice.exception;

public class CatalogServiceUnavailableException extends RuntimeException {

    public CatalogServiceUnavailableException() {
        super("Catalog service is currently unavailable");
    }
}