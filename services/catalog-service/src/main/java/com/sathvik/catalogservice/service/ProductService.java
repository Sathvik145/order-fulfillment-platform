package com.sathvik.catalogservice.service;

import com.sathvik.catalogservice.dto.CreateProductRequest;
import com.sathvik.catalogservice.dto.ProductResponse;
import com.sathvik.catalogservice.entity.Product;
import com.sathvik.catalogservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import com.sathvik.catalogservice.exception.ProductNotFoundException;


import java.time.Instant;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(CreateProductRequest request) {

        Instant now = Instant.now();

        Product product = new Product(
                request.productId(),
                request.name(),
                request.description(),
                request.price(),
                true,
                now,
                now
        );

        Product savedProduct = productRepository.save(product);

        return new ProductResponse(
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.isActive(),
                savedProduct.getCreatedAt(),
                savedProduct.getUpdatedAt()
        );
    }
    public ProductResponse getProductById(String productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId)
                );

        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}