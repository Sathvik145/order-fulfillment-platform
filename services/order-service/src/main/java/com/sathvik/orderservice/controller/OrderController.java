package com.sathvik.orderservice.controller;

import com.sathvik.orderservice.dto.CreateOrderRequest;
import com.sathvik.orderservice.dto.OrderResponse;
import com.sathvik.orderservice.event.OrderCreatedEvent;
import com.sathvik.orderservice.ratelimit.OrderRateLimiter;
import com.sathvik.orderservice.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderRateLimiter orderRateLimiter;

    public OrderController(
            OrderService orderService,
            OrderRateLimiter orderRateLimiter
    ) {
        this.orderService = orderService;
        this.orderRateLimiter = orderRateLimiter;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {

        boolean allowed =
                orderRateLimiter.isAllowed(request.userId());

        if (!allowed) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(
                            "Rate limit exceeded. Maximum 5 orders per minute."
                    );
        }

        OrderCreatedEvent response =
                orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(
            @PathVariable UUID orderId
    ) {
        return orderService.getOrderById(orderId);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUserId(
            @PathVariable String userId
    ) {
        return orderService.getOrdersByUserId(userId);
    }
}