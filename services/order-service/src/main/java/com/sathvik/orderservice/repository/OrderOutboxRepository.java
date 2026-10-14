package com.sathvik.orderservice.repository;

import com.sathvik.orderservice.entity.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderOutboxRepository
        extends JpaRepository<OrderOutbox, UUID> {

    List<OrderOutbox>
    findTop50ByStatusOrderByCreatedAtAsc(String status);
}