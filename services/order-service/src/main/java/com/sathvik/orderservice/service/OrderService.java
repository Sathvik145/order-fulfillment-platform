package com.sathvik.orderservice.service;

import com.sathvik.orderservice.dto.CreateOrderRequest;
import com.sathvik.orderservice.dto.OrderResponse;
import com.sathvik.orderservice.entity.Order;
import com.sathvik.orderservice.entity.OrderItem;
import com.sathvik.orderservice.event.OrderCreatedEvent;
import com.sathvik.orderservice.event.OrderItemEvent;
import com.sathvik.orderservice.exception.OrderNotFoundException;
import com.sathvik.orderservice.producer.OrderEventProducer;
import com.sathvik.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sathvik.orderservice.dto.OrderItemResponse;
import com.sathvik.orderservice.client.CatalogClient;
import com.sathvik.orderservice.dto.CatalogProductResponse;
import java.util.UUID;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderEventProducer orderEventProducer;
    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;

    public OrderService(
            OrderEventProducer orderEventProducer,
            OrderRepository orderRepository,
            CatalogClient catalogClient
    ) {
        this.orderEventProducer = orderEventProducer;
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
    }

    @Transactional
    public OrderCreatedEvent createOrder(CreateOrderRequest request) {

        UUID orderId = UUID.randomUUID();
        String eventId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Order order = new Order(
                orderId,
                request.userId(),
                "CREATED",
                BigDecimal.ZERO,
                now,
                now
        );

        BigDecimal orderTotal = BigDecimal.ZERO;

        List<OrderItemEvent> itemEvents = new ArrayList<>();

        for (var itemRequest : request.items()) {

            CatalogProductResponse product =
                    catalogClient.getProduct(itemRequest.productId());

            if (product == null) {
                throw new RuntimeException(
                        "Unable to fetch product: " + itemRequest.productId()
                );
            }

            if (!product.active()) {
                throw new RuntimeException(
                        "Product is inactive: " + itemRequest.productId()
                );
            }

            BigDecimal unitPrice = product.price();

            BigDecimal itemTotal =
                    unitPrice.multiply(
                            BigDecimal.valueOf(itemRequest.quantity())
                    );

            OrderItem orderItem = new OrderItem(
                    UUID.randomUUID(),
                    order,
                    itemRequest.productId(),
                    itemRequest.quantity(),
                    unitPrice,
                    itemTotal
            );

            order.addItem(orderItem);

            orderTotal = orderTotal.add(itemTotal);

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

        orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                orderId.toString(),
                request.userId(),
                itemEvents,
                orderTotal,
                "CREATED",
                now
        );

        orderEventProducer.publishOrderCreated(event);

        return event;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(orderId)
                );

        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(String userId) {

        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional
    public void markInventoryReserved(UUID orderId) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.setStatus("INVENTORY_RESERVED");
    }
    @Transactional
    public void cancelOrder(UUID orderId) {

        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.setStatus("CANCELLED");
    }



    private OrderResponse toOrderResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
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