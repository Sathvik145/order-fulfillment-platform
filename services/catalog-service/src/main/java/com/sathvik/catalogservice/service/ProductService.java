package com.sathvik.catalogservice.service;

import com.sathvik.catalogservice.dto.CreateProductRequest;
import com.sathvik.catalogservice.dto.ProductResponse;
import com.sathvik.catalogservice.entity.Product;
import com.sathvik.catalogservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import com.sathvik.catalogservice.exception.ProductNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import com.sathvik.catalogservice.dto.UpdateProductRequest;
import org.springframework.cache.annotation.CachePut;


import java.time.Instant;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @CacheEvict(
            value = "products",
            key = "#request.productId()"
    )
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

    @CachePut(
            value = "products",
            key = "#productId"
    )
    public ProductResponse updateProduct(
            String productId,
            UpdateProductRequest request
    ) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ProductNotFoundException(productId)
                );

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setActive(request.active());
        product.setUpdatedAt(Instant.now());

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


    @Cacheable(
            value = "products",
            key = "#productId"
    )
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