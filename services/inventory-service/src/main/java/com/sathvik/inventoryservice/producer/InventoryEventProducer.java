package com.sathvik.inventoryservice.producer;

import com.sathvik.inventoryservice.event.InventoryFailedEvent;
import com.sathvik.inventoryservice.event.InventoryReservedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInventoryReserved(
            InventoryReservedEvent event
    ) {

        kafkaTemplate.send(
                "inventory.reserved",
                event.orderId().toString(),
                event
        );
    }

    public void publishInventoryFailed(
            InventoryFailedEvent event
    ) {

        kafkaTemplate.send(
                "inventory.failed",
                event.orderId().toString(),
                event
        );
    }
}