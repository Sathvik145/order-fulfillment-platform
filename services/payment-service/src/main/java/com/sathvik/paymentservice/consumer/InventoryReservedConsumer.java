package com.sathvik.paymentservice.consumer;

import com.sathvik.paymentservice.event.InventoryReservedEvent;
import com.sathvik.paymentservice.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryReservedConsumer {

    private final PaymentService paymentService;

    public InventoryReservedConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(
            topics = "inventory.reserved",
            groupId = "payment-service-group"
    )
    public void consume(InventoryReservedEvent event) {

        System.out.println(
                "Inventory Reserved Event received by Payment Service"
        );

        System.out.println("Order ID: " + event.orderId());
        System.out.println("Amount: " + event.totalAmount());

        paymentService.processPayment(event);
    }
}