package com.sathvik.orderservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

    private final Counter ordersCreated;
    private final Counter ordersConfirmed;
    private final Counter ordersCancelled;
    private final Counter rateLimitRejections;

    public OrderMetrics(MeterRegistry meterRegistry) {

        this.ordersCreated =
                Counter.builder("orders.created")
                        .description("Total number of orders created")
                        .register(meterRegistry);

        this.ordersConfirmed =
                Counter.builder("orders.confirmed")
                        .description("Total number of orders confirmed")
                        .register(meterRegistry);

        this.ordersCancelled =
                Counter.builder("orders.cancelled")
                        .description("Total number of orders cancelled")
                        .register(meterRegistry);

        this.rateLimitRejections =
                Counter.builder("rate.limit.rejections")
                        .description("Total number of rejected order requests")
                        .register(meterRegistry);
    }

    public void incrementOrdersCreated() {
        ordersCreated.increment();
    }

    public void incrementOrdersConfirmed() {
        ordersConfirmed.increment();
    }

    public void incrementOrdersCancelled() {
        ordersCancelled.increment();
    }

    public void incrementRateLimitRejections() {
        rateLimitRejections.increment();
    }
}