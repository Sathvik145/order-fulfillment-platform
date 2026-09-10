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

import java.util.Comparator;
import java.util.List;

import java.time.Instant;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
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
    }
}