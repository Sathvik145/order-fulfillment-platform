package com.sathvik.orderservice.outbox;

import com.sathvik.orderservice.entity.OrderOutbox;
import com.sathvik.orderservice.event.OrderCreatedEvent;
import com.sathvik.orderservice.producer.OrderEventProducer;
import com.sathvik.orderservice.repository.OrderOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class OrderOutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    OrderOutboxPublisher.class
            );

    private final OrderOutboxRepository outboxRepository;

    private final OrderEventProducer orderEventProducer;

    private final ObjectMapper objectMapper;

    public OrderOutboxPublisher(
            OrderOutboxRepository outboxRepository,
            OrderEventProducer orderEventProducer,
            ObjectMapper objectMapper
    ) {

        this.outboxRepository =
                outboxRepository;

        this.orderEventProducer =
                orderEventProducer;

        this.objectMapper =
                objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OrderOutbox> events =
                outboxRepository
                        .findTop50ByStatusOrderByCreatedAtAsc(
                                "PENDING"
                        );

        for (OrderOutbox outbox : events) {

            try {

                if (!"order.created"
                        .equals(outbox.getEventType())) {

                    log.warn(
                            "Unknown Order outbox event type: {}",
                            outbox.getEventType()
                    );

                    continue;
                }

                OrderCreatedEvent event =
                        objectMapper.readValue(
                                outbox.getPayload(),
                                OrderCreatedEvent.class
                        );

                /*
                 * Publish to Kafka.
                 *
                 * publishOrderCreated waits for Kafka ACK.
                 */
                orderEventProducer
                        .publishOrderCreated(event);

                /*
                 * Kafka publish succeeded.
                 */
                outbox.markPublished();

                outboxRepository.save(outbox);

                log.info(
                        "Order outbox event published. "
                                + "Event ID: {}, Order ID: {}",
                        outbox.getId(),
                        outbox.getAggregateId()
                );

            } catch (Exception ex) {

                /*
                 * IMPORTANT:
                 *
                 * Don't mark PUBLISHED.
                 *
                 * It remains PENDING and the scheduler
                 * will retry later.
                 */

                log.error(
                        "Failed to publish Order outbox event {}: {}",
                        outbox.getId(),
                        ex.getMessage()
                );
            }
        }
    }
}