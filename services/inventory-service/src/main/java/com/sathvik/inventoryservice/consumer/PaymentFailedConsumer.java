package com.sathvik.inventoryservice.consumer;

import com.sathvik.inventoryservice.event.PaymentFailedEvent;
import com.sathvik.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentFailedConsumer {

    private final InventoryService inventoryService;

    public PaymentFailedConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(
            topics = "payment.failed",
            groupId = "inventory-payment-group",
            properties = {
                    "spring.json.value.default.type=com.sathvik.inventoryservice.event.PaymentFailedEvent"
            }
    )
    public void consume(PaymentFailedEvent event) {

        System.out.println(
                "Inventory received payment.failed for order: "
                        + event.orderId()
        );

        inventoryService.releaseReservation(event);
    }
}