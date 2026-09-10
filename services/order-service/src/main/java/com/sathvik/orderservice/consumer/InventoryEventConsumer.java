package com.sathvik.orderservice.consumer;

import com.sathvik.orderservice.event.InventoryFailedEvent;
import com.sathvik.orderservice.event.InventoryReservedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.sathvik.orderservice.service.OrderService;

@Component
public class InventoryEventConsumer {

    private final OrderService orderService;

    public InventoryEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    private static final Logger log =
            LoggerFactory.getLogger(InventoryEventConsumer.class);

    @KafkaListener(
            topics = "inventory.reserved",
            groupId = "order-service-inventory-group",
            properties = {
                    "spring.json.value.default.type=com.sathvik.orderservice.event.InventoryReservedEvent"
            }
    )
    public void consumeInventoryReserved(
            InventoryReservedEvent event
    ) {

        log.info(
                "Order Service received inventory.reserved for order: {}",
                event.orderId()
        );

        orderService.markInventoryReserved(event.orderId());

        log.info(
                "Order {} status updated to INVENTORY_RESERVED",
                event.orderId()
        );
    }

    @KafkaListener(
            topics = "inventory.failed",
            groupId = "order-service-inventory-group",
            properties = {
                    "spring.json.value.default.type=com.sathvik.orderservice.event.InventoryFailedEvent"
            }
    )
    public void consumeInventoryFailed(
            InventoryFailedEvent event
    ) {

        log.warn(
                "Order Service received inventory.failed for order {}. Reason: {}",
                event.orderId(),
                event.reason()
        );

        orderService.cancelOrder(event.orderId());

        log.info(
                "Order {} status updated to CANCELLED",
                event.orderId()
        );
    }
}