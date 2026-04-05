package com.example.notification_service.consumer;

import com.example.notification_service.event.OrderCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class NotificationEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @KafkaListener(topics = "orders.created", groupId = "notification-group")
    // notification-group — DIFFERENT from inventory-service-group
    // Kafka delivers every message to BOTH groups independently
    public void handleOrderCreated(String message) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);

            logger.info("[notification-service] ORDER_CREATED received: orderId={} customerId={} eventVersion={}",
                    event.getOrderId(), event.getCustomerId(), event.getEventVersion());
            // log eventVersion — confirms the new field is flowing correctly

            // simulate sending notification
            logger.info("[notification-service] ✉️ Notification sent to customer {} for orderId={} — total={}",
                    event.getCustomerId(), event.getOrderId(), event.getTotalAmount());
            // Day 15+: replace with real email/SMS via notification provider

        } catch (Exception e) {
            logger.error("[notification-service] Failed to process: {}", e.getMessage());
        }
    }
}