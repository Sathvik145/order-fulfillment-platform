package com.sathvik.inventoryservice.repository;

import com.sathvik.inventoryservice.entity.InventoryOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.UUID;

public interface InventoryOutboxRepository
        extends JpaRepository<InventoryOutbox, UUID> {

    List<InventoryOutbox> findTop50ByStatusOrderByCreatedAtAsc(String status);
}