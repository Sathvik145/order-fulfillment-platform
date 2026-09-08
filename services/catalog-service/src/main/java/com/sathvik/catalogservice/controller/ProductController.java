package com.sathvik.catalogservice.controller;

import com.sathvik.catalogservice.dto.CreateProductRequest;
import com.sathvik.catalogservice.dto.ProductResponse;
import com.sathvik.catalogservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        return productService.createProduct(request);
    }
    @GetMapping("/{productId}")
    public ProductResponse getProductById(
            @PathVariable String productId
    ) {
        return productService.getProductById(productId);
    }
}