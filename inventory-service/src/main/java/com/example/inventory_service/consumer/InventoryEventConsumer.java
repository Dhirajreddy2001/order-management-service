package com.example.inventory_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.inventory_service.event.OrderCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class InventoryEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    
    @KafkaListener(topics = "orders.created", groupId = "inventory-service-group")
    public void handleOrderCreatedEvent(String message) {
        // receive as raw String — StringDeserializer has no trusted packages issue
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            // manually parse JSON string → OrderCreatedEvent object

            logger.info("[inventory-service] Received ORDER_CREATED: orderId={}",
                    event.getOrderId());

            if (event.getItems() == null || event.getItems().isEmpty()) {
                logger.warn("Order {} has no items — skipping", event.getOrderId());
                return;
            }

            event.getItems()
                    .forEach(item -> logger.info(
                            "[inventory-service] Reserving stock: sku={} quantity={} for orderId={}",
                            item.getSku(), item.getQuantity(), event.getOrderId()));

            logger.info("[inventory-service] Done: orderId={} — {} items processed",
                    event.getOrderId(), event.getItems().size());

        } catch (Exception e) {
            // catch all — prevents consumer from getting stuck retrying bad messages
            logger.error("[inventory-service] Failed to process message: {}", e.getMessage());
        }
    }
}