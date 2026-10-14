package com.sathvik.paymentservice.outbox;

import com.sathvik.paymentservice.entity.PaymentOutbox;
import com.sathvik.paymentservice.event.PaymentCompletedEvent;
import com.sathvik.paymentservice.event.PaymentFailedEvent;
import com.sathvik.paymentservice.producer.PaymentEventProducer;
import com.sathvik.paymentservice.repository.PaymentOutboxRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class PaymentOutboxPublisher {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;

    public PaymentOutboxPublisher(
            PaymentOutboxRepository paymentOutboxRepository,
            PaymentEventProducer paymentEventProducer,
            ObjectMapper objectMapper
    ) {
        this.paymentOutboxRepository = paymentOutboxRepository;
        this.paymentEventProducer = paymentEventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<PaymentOutbox> events =
                paymentOutboxRepository
                        .findTop50ByStatusOrderByCreatedAtAsc(
                                "PENDING"
                        );

        for (PaymentOutbox outbox : events) {

            try {

                if ("payment.completed".equals(
                        outbox.getEventType()
                )) {

                    PaymentCompletedEvent event =
                            objectMapper.readValue(
                                    outbox.getPayload(),
                                    PaymentCompletedEvent.class
                            );

                    paymentEventProducer
                            .publishPaymentCompleted(event);

                } else if ("payment.failed".equals(
                        outbox.getEventType()
                )) {

                    PaymentFailedEvent event =
                            objectMapper.readValue(
                                    outbox.getPayload(),
                                    PaymentFailedEvent.class
                            );

                    paymentEventProducer
                            .publishPaymentFailed(event);

                } else {

                    System.out.println(
                            "Unknown payment event type: "
                                    + outbox.getEventType()
                    );

                    continue;
                }

                outbox.markPublished();

                paymentOutboxRepository.save(outbox);

                System.out.println(
                        "Payment outbox event published. Type: "
                                + outbox.getEventType()
                                + ", Order ID: "
                                + outbox.getAggregateId()
                );

            } catch (Exception ex) {

                System.err.println(
                        "Failed to publish payment outbox event: "
                                + outbox.getId()
                );

                ex.printStackTrace();
            }
        }
    }
}