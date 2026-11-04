package com.sathvik.inventoryservice.repository;

import com.sathvik.inventoryservice.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, UUID> {

    List<InventoryReservation> findByOrderIdAndStatus(
            UUID orderId,
            String status
    );
}