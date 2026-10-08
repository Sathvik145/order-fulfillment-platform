package com.sathvik.inventoryservice.outbox;

import com.sathvik.inventoryservice.entity.InventoryOutbox;
import com.sathvik.inventoryservice.event.InventoryReservedEvent;
import com.sathvik.inventoryservice.producer.InventoryEventProducer;
import com.sathvik.inventoryservice.repository.InventoryOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import com.sathvik.inventoryservice.event.InventoryFailedEvent;

import java.util.List;

@Component
public class InventoryOutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryOutboxPublisher.class);

    private final InventoryOutboxRepository outboxRepository;
    private final InventoryEventProducer inventoryEventProducer;
    private final ObjectMapper objectMapper;

    public InventoryOutboxPublisher(
            InventoryOutboxRepository outboxRepository,
            InventoryEventProducer inventoryEventProducer,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.inventoryEventProducer = inventoryEventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<InventoryOutbox> events =
                outboxRepository
                        .findTop50ByStatusOrderByCreatedAtAsc("PENDING");

        for (InventoryOutbox outbox : events) {

            try {

                if ("inventory.reserved".equals(outbox.getEventType())) {

                    InventoryReservedEvent event =
                            objectMapper.readValue(
                                    outbox.getPayload(),
                                    InventoryReservedEvent.class
                            );

                    inventoryEventProducer
                            .publishInventoryReserved(event);

                } else if ("inventory.failed".equals(outbox.getEventType())) {

                    InventoryFailedEvent event =
                            objectMapper.readValue(
                                    outbox.getPayload(),
                                    InventoryFailedEvent.class
                            );

                    inventoryEventProducer
                            .publishInventoryFailed(event);

                } else {

                    log.warn(
                            "Unknown outbox event type: {}",
                            outbox.getEventType()
                    );

                    continue;
                }

                outbox.markPublished();
                outboxRepository.save(outbox);

                log.info(
                        "Outbox event published. Type: {}, Event ID: {}, Order ID: {}",
                        outbox.getEventType(),
                        outbox.getId(),
                        outbox.getAggregateId()
                );

            } catch (Exception ex) {

                log.error(
                        "Failed to publish outbox event {}: {}",
                        outbox.getId(),
                        ex.getMessage()
                );
            }
        }
    }
}