package com.sathvik.inventoryservice.service;

import com.sathvik.inventoryservice.dto.InventoryResponse;
import com.sathvik.inventoryservice.dto.StockRequest;
import com.sathvik.inventoryservice.entity.Inventory;
import com.sathvik.inventoryservice.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sathvik.inventoryservice.event.OrderCreatedEvent;
import com.sathvik.inventoryservice.event.OrderItemEvent;
import com.sathvik.inventoryservice.exception.InsufficientStockException;
import com.sathvik.inventoryservice.exception.InventoryNotFoundException;
import com.sathvik.inventoryservice.entity.InventoryOutbox;
import com.sathvik.inventoryservice.event.InventoryReservedEvent;
import com.sathvik.inventoryservice.repository.InventoryOutboxRepository;
import tools.jackson.databind.ObjectMapper;
import com.sathvik.inventoryservice.event.InventoryFailedEvent;

import java.time.Instant;
import java.util.UUID;

import java.util.Comparator;
import java.util.List;

import java.time.Instant;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryOutboxRepository inventoryOutboxRepository;
    private final ObjectMapper objectMapper;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryOutboxRepository inventoryOutboxRepository,
            ObjectMapper objectMapper
    ) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryOutboxRepository = inventoryOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public InventoryResponse addOrUpdateStock(StockRequest request) {

        Inventory inventory = inventoryRepository
                .findById(request.productId())
                .orElse(
                        new Inventory(
                                request.productId(),
                                0,
                                0,
                                Instant.now()
                        )
                );

        inventory.setAvailableQuantity(request.quantity());

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        return new InventoryResponse(
                savedInventory.getProductId(),
                savedInventory.getAvailableQuantity(),
                savedInventory.getReservedQuantity(),
                savedInventory.getUpdatedAt()
        );
    }

    @Transactional
    public void reserveStock(OrderCreatedEvent event) {

        List<OrderItemEvent> items = event.items()
                .stream()
                .sorted(Comparator.comparing(OrderItemEvent::productId))
                .toList();

        for (OrderItemEvent item : items) {

            Inventory inventory = inventoryRepository
                    .findByProductIdForUpdate(item.productId())
                    .orElseThrow(
                            () -> new InventoryNotFoundException(
                                    item.productId()
                            )
                    );

            if (inventory.getAvailableQuantity() < item.quantity()) {

                throw new InsufficientStockException(
                        item.productId(),
                        item.quantity(),
                        inventory.getAvailableQuantity()
                );
            }

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity()
                            - item.quantity()
            );

            inventory.setReservedQuantity(
                    inventory.getReservedQuantity()
                            + item.quantity()
            );
        }

        InventoryReservedEvent reservedEvent =
                new InventoryReservedEvent(
                        UUID.randomUUID(),
                        event.orderId(),
                        event.userId(),
                        event.items(),
                        event.totalAmount(),
                        Instant.now()
                );

        String payload;

        try {
            payload = objectMapper.writeValueAsString(reservedEvent);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to serialize inventory.reserved event",
                    ex
            );
        }

        InventoryOutbox outboxEvent =
                new InventoryOutbox(
                        UUID.randomUUID(),
                        event.orderId(),
                        "inventory.reserved",
                        payload,
                        "PENDING",
                        Instant.now(),
                        null
                );

        inventoryOutboxRepository.save(outboxEvent);


    }
    @Transactional
    public void createInventoryFailedOutbox(
            OrderCreatedEvent event,
            String reason
    ) {

        InventoryFailedEvent failedEvent =
                new InventoryFailedEvent(
                        UUID.randomUUID(),
                        event.orderId(),
                        reason,
                        Instant.now()
                );

        String payload;

        try {
            payload = objectMapper.writeValueAsString(failedEvent);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to serialize inventory.failed event",
                    ex
            );
        }

        InventoryOutbox outboxEvent =
                new InventoryOutbox(
                        UUID.randomUUID(),
                        event.orderId(),
                        "inventory.failed",
                        payload,
                        "PENDING",
                        Instant.now(),
                        null
                );

        inventoryOutboxRepository.save(outboxEvent);
    }
}