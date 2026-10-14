package com.sathvik.paymentservice.service;

import com.sathvik.paymentservice.entity.Payment;
import com.sathvik.paymentservice.entity.PaymentOutbox;
import com.sathvik.paymentservice.entity.PaymentStatus;
import com.sathvik.paymentservice.event.InventoryReservedEvent;
import com.sathvik.paymentservice.event.PaymentCompletedEvent;
import com.sathvik.paymentservice.event.PaymentFailedEvent;
import com.sathvik.paymentservice.repository.PaymentOutboxRepository;
import com.sathvik.paymentservice.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentOutboxRepository paymentOutboxRepository,
            ObjectMapper objectMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentOutboxRepository = paymentOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processPayment(InventoryReservedEvent event) {

        // Idempotency check
        if (paymentRepository.existsByOrderId(event.orderId())) {
            System.out.println(
                    "Payment already exists for order: " + event.orderId()
            );
            return;
        }

        Instant now = Instant.now();
        UUID paymentId = UUID.randomUUID();

        // IMPORTANT:
        // Use the Payment returned by save()
        // because this is the managed Hibernate entity.
        Payment payment = paymentRepository.save(
                new Payment(
                        paymentId,
                        event.orderId(),
                        event.userId(),
                        event.totalAmount(),
                        PaymentStatus.PENDING,
                        now,
                        now
                )
        );

        // Deterministic failure simulation
        boolean shouldFail =
                event.userId().startsWith("PAYMENT-FAIL-");

        try {

            if (shouldFail) {

                payment.setStatus(PaymentStatus.FAILED);

                PaymentFailedEvent failedEvent =
                        new PaymentFailedEvent(
                                UUID.randomUUID(),
                                event.orderId(),
                                paymentId,
                                event.userId(),
                                event.totalAmount(),
                                "Simulated payment failure",
                                Instant.now()
                        );

                String payload =
                        objectMapper.writeValueAsString(failedEvent);

                PaymentOutbox outbox =
                        new PaymentOutbox(
                                UUID.randomUUID(),
                                event.orderId(),
                                "payment.failed",
                                payload,
                                "PENDING",
                                Instant.now(),
                                null
                        );

                paymentOutboxRepository.save(outbox);

                System.out.println(
                        "Payment FAILED for order: "
                                + event.orderId()
                );

            } else {

                payment.setStatus(PaymentStatus.COMPLETED);

                PaymentCompletedEvent completedEvent =
                        new PaymentCompletedEvent(
                                UUID.randomUUID(),
                                event.orderId(),
                                paymentId,
                                event.userId(),
                                event.totalAmount(),
                                Instant.now()
                        );

                String payload =
                        objectMapper.writeValueAsString(completedEvent);

                PaymentOutbox outbox =
                        new PaymentOutbox(
                                UUID.randomUUID(),
                                event.orderId(),
                                "payment.completed",
                                payload,
                                "PENDING",
                                Instant.now(),
                                null
                        );

                paymentOutboxRepository.save(outbox);

                System.out.println(
                        "Payment COMPLETED for order: "
                                + event.orderId()
                );
            }

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to create payment outbox event",
                    ex
            );
        }
    }
}