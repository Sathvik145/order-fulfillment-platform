package com.sathvik.paymentservice.repository;

import com.sathvik.paymentservice.entity.PaymentOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentOutboxRepository
        extends JpaRepository<PaymentOutbox, UUID> {

    List<PaymentOutbox> findTop50ByStatusOrderByCreatedAtAsc(String status);
}