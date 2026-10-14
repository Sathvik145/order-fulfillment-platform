package com.sathvik.orderservice.producer;

import com.sathvik.orderservice.event.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final String TOPIC = "order.created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {

        try {

            kafkaTemplate.send(
                    TOPIC,
                    event.orderId().toString(),
                    event
            ).get();

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Interrupted while publishing order.created",
                    ex
            );

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to publish order.created",
                    ex
            );
        }
    }
}