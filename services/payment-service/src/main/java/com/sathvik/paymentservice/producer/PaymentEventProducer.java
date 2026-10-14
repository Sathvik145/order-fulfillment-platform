package com.sathvik.paymentservice.producer;

import com.sathvik.paymentservice.event.PaymentCompletedEvent;
import com.sathvik.paymentservice.event.PaymentFailedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentCompleted(
            PaymentCompletedEvent event
    ) {

        try {
            kafkaTemplate
                    .send(
                            "payment.completed",
                            event.orderId().toString(),
                            event
                    )
                    .get();

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Interrupted while publishing payment.completed",
                    ex
            );

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Failed to publish payment.completed",
                    ex
            );
        }
    }

    public void publishPaymentFailed(
            PaymentFailedEvent event
    ) {

        try {
            kafkaTemplate
                    .send(
                            "payment.failed",
                            event.orderId().toString(),
                            event
                    )
                    .get();

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Interrupted while publishing payment.failed",
                    ex
            );

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Failed to publish payment.failed",
                    ex
            );
        }
    }
}