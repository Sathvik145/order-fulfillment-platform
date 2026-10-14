package com.sathvik.orderservice.service;

import com.sathvik.orderservice.client.CatalogClient;
import com.sathvik.orderservice.dto.CatalogProductResponse;
import com.sathvik.orderservice.dto.CreateOrderRequest;
import com.sathvik.orderservice.dto.OrderItemRequest;
import com.sathvik.orderservice.dto.OrderItemResponse;
import com.sathvik.orderservice.dto.OrderResponse;
import com.sathvik.orderservice.entity.Order;
import com.sathvik.orderservice.entity.OrderItem;
import com.sathvik.orderservice.entity.OrderOutbox;
import com.sathvik.orderservice.event.OrderCreatedEvent;
import com.sathvik.orderservice.event.OrderItemEvent;
import com.sathvik.orderservice.exception.OrderNotFoundException;
import com.sathvik.orderservice.repository.OrderOutboxRepository;
import com.sathvik.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;
    private final OrderOutboxRepository orderOutboxRepository;
    private final ObjectMapper objectMapper;

    public OrderService(
            OrderRepository orderRepository,
            CatalogClient catalogClient,
            OrderOutboxRepository orderOutboxRepository,
            ObjectMapper objectMapper
    ) {
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
        this.orderOutboxRepository = orderOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderCreatedEvent createOrder(CreateOrderRequest request) {

        UUID orderId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();

        Order order = new Order(
                orderId,
                request.userId(),
                "CREATED",
                BigDecimal.ZERO,
                now,
                now
        );

        List<OrderItemEvent> itemEvents = new ArrayList<>();
        BigDecimal orderTotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {

            CatalogProductResponse product =
                    catalogClient.getProduct(
                            itemRequest.productId()
                    );

            if (!product.active()) {
                throw new RuntimeException(
                        "Product is inactive: "
                                + itemRequest.productId()
                );
            }

            BigDecimal unitPrice = product.price();

            BigDecimal itemTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(
                                    itemRequest.quantity()
                            )
                    );

            OrderItem orderItem =
                    new OrderItem(
                            UUID.randomUUID(),
                            order,
                            itemRequest.productId(),
                            itemRequest.quantity(),
                            unitPrice,
                            itemTotal
                    );

            order.addItem(orderItem);

            orderTotal =
                    orderTotal.add(itemTotal);

            itemEvents.add(
                    new OrderItemEvent(
                            itemRequest.productId(),
                            itemRequest.quantity(),
                            unitPrice,
                            itemTotal
                    )
            );
        }

        order.setTotalAmount(orderTotal);

        /*
         * DB WRITE #1
         *
         * Save order + order items.
         */
        orderRepository.save(order);

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        eventId.toString(),
                        orderId.toString(),
                        request.userId(),
                        itemEvents,
                        orderTotal,
                        "CREATED",
                        now
                );

        /*
         * Convert the event into JSON so it can be
         * stored durably inside the outbox table.
         */
        String payload;

        try {

            payload =
                    objectMapper.writeValueAsString(event);

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Failed to serialize order.created event",
                    ex
            );
        }

        OrderOutbox outboxEvent =
                new OrderOutbox(
                        UUID.randomUUID(),
                        orderId,
                        "order.created",
                        payload,
                        "PENDING",
                        Instant.now(),
                        null
                );

        /*
         * DB WRITE #2
         *
         * Save the event that must eventually
         * be published to Kafka.
         */
        orderOutboxRepository.save(outboxEvent);

        /*
         * Because this whole method is @Transactional:
         *
         * order + order_items
         * +
         * order_outbox
         *
         * commit together.
         *
         * We DO NOT directly publish to Kafka here.
         */

        return event;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(
                                () -> new OrderNotFoundException(orderId)
                        );

        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(
            String userId
    ) {

        return orderRepository
                .findByUserId(userId)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional
    public void markInventoryReserved(UUID orderId) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(
                                () -> new OrderNotFoundException(orderId)
                        );

        order.setStatus(
                "INVENTORY_RESERVED"
        );
    }

    @Transactional
    public void cancelOrder(UUID orderId) {

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(
                                () -> new OrderNotFoundException(orderId)
                        );

        order.setStatus(
                "CANCELLED"
        );
    }

    private OrderResponse toOrderResponse(
            Order order
    ) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(
                                item ->
                                        new OrderItemResponse(
                                                item.getId(),
                                                item.getProductId(),
                                                item.getQuantity(),
                                                item.getUnitPrice(),
                                                item.getTotalPrice()
                                        )
                        )
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}