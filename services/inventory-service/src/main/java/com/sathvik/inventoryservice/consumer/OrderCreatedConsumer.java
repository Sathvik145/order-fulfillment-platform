package com.sathvik.inventoryservice.consumer;

import com.sathvik.inventoryservice.event.InventoryFailedEvent;
import com.sathvik.inventoryservice.event.InventoryReservedEvent;
import com.sathvik.inventoryservice.event.OrderCreatedEvent;
import com.sathvik.inventoryservice.exception.InsufficientStockException;
import com.sathvik.inventoryservice.exception.InventoryNotFoundException;
import com.sathvik.inventoryservice.producer.InventoryEventProducer;
import com.sathvik.inventoryservice.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrderCreatedConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;

    public OrderCreatedConsumer(
            InventoryService inventoryService,
            InventoryEventProducer inventoryEventProducer
    ) {
        this.inventoryService = inventoryService;
        this.inventoryEventProducer = inventoryEventProducer;
    }

    @KafkaListener(
            topics = "order.created",
            groupId = "inventory-service-group"
    )
    public void consume(OrderCreatedEvent event) {

        log.info(
                "Inventory Service received order.created for order: {}",
                event.orderId()
        );

        try {

            inventoryService.reserveStock(event);

            InventoryReservedEvent reservedEvent =
                    new InventoryReservedEvent(
                            UUID.randomUUID(),
                            event.orderId(),
                            event.userId(),
                            event.items(),
                            event.totalAmount(),
                            Instant.now()
                    );

            inventoryEventProducer
                    .publishInventoryReserved(reservedEvent);

            log.info(
                    "Inventory reserved successfully for order: {}",
                    event.orderId()
            );

        } catch (InventoryNotFoundException |
                 InsufficientStockException ex) {

            InventoryFailedEvent failedEvent =
                    new InventoryFailedEvent(
                            UUID.randomUUID(),
                            event.orderId(),
                            ex.getMessage(),
                            Instant.now()
                    );

            inventoryEventProducer
                    .publishInventoryFailed(failedEvent);

            log.warn(
                    "Inventory reservation failed for order {}: {}",
                    event.orderId(),
                    ex.getMessage()
            );
        }
    }
}