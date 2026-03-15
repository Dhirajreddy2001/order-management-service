package com.example.inventory_service.consumer;

import com.example.inventory_service.service.StockService;

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

    private final StockService stockService;

    public InventoryEventConsumer(StockService stockService) {
        this.stockService = stockService;
    }

    @KafkaListener(topics = "orders.created", groupId = "inventory-service-group")
    public void handleOrderCreatedEvent(String message) {
        // receive as raw String — StringDeserializer has no trusted packages issue
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);

            logger.info("[inventory-service] Received ORDER_CREATED: orderId={}",
                    event.getOrderId());

            if (event.getItems() == null || event.getItems().isEmpty()) {
                logger.warn("Order {} has no items — skipping", event.getOrderId());
                return;
            }

            event.getItems()
                    .forEach(item -> stockService.reserveStock(item.getSku(), item.getQuantity(), event.getOrderId()));

            logger.info("[inventory-service] Done: orderId={} — {} items processed",
                    event.getOrderId(), event.getItems().size());

        } catch (Exception e) {
            // catch all — prevents consumer from getting stuck retrying bad messages
            logger.error("[inventory-service] Failed to process message: {}", e.getMessage());
        }
    }
}