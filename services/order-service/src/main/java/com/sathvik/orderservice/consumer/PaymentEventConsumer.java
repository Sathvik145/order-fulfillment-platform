package com.sathvik.orderservice.consumer;

import com.sathvik.orderservice.event.PaymentCompletedEvent;
import com.sathvik.orderservice.event.PaymentFailedEvent;
import com.sathvik.orderservice.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private final OrderService orderService;

    public PaymentEventConsumer(
            OrderService orderService
    ) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = "payment.completed",
            groupId = "order-service-payment-group",
            properties = {
                    "spring.json.value.default.type=com.sathvik.orderservice.event.PaymentCompletedEvent"
            }
    )
    public void handlePaymentCompleted(
            PaymentCompletedEvent event
    ) {

        System.out.println(
                "Payment COMPLETED event received"
        );

        System.out.println(
                "Order ID: " + event.orderId()
        );

        orderService.confirmOrder(
                event.orderId()
        );
    }

    @KafkaListener(
            topics = "payment.failed",
            groupId = "order-service-payment-group",
            properties = {
                    "spring.json.value.default.type=com.sathvik.orderservice.event.PaymentFailedEvent"
            }
    )
    public void handlePaymentFailed(
            PaymentFailedEvent event
    ) {

        System.out.println(
                "Payment FAILED event received"
        );

        System.out.println(
                "Order ID: " + event.orderId()
        );

        System.out.println(
                "Reason: " + event.reason()
        );

        orderService.cancelOrder(
                event.orderId()
        );
    }
}