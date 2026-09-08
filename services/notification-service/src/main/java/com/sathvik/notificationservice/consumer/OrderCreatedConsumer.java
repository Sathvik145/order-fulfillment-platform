package com.sathvik.notificationservice.consumer;

import com.sathvik.notificationservice.event.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    @KafkaListener(
            topics = "order.created",
            groupId = "notification-service-group"
    )
    public void consume(OrderCreatedEvent event) {

        System.out.println("Order Created Event Received");

        System.out.println("Event ID: " + event.eventId());
        System.out.println("Order ID: " + event.orderId());
        System.out.println("User ID: " + event.userId());

        System.out.println("Items:");

        event.items().forEach(item -> {

            System.out.println(
                    "Product: " + item.productId()
                            + ", Quantity: " + item.quantity()
                            + ", Unit Price: " + item.unitPrice()
                            + ", Total Price: " + item.totalPrice()
            );
        });

        System.out.println("Order Total: " + event.totalAmount());
        System.out.println("Status: " + event.status());
        System.out.println("Created At: " + event.createdAt());
    }
}