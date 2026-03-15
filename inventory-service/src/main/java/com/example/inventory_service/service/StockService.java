package com.example.inventory_service.service;

import com.example.inventory_service.event.OrderRejectedEvent;
import com.example.inventory_service.repository.StockRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final StockRepository stockRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    // inject KafkaTemplate — used to publish ORDER_REJECTED events
    private final ObjectMapper objectMapper = new ObjectMapper();
    // serialize OrderRejectedEvent to JSON string manually

    public StockService(StockRepository stockRepository,
                        KafkaTemplate<String, String> kafkaTemplate) {
        this.stockRepository = stockRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void reserveStock(String sku, int quantity, Long orderId) {
        logger.info("Reserving stock for SKU={} qty={} orderId={}", sku, quantity, orderId);

        var stockOpt = stockRepository.findBySku(sku);
        // returns Optional<StockEntity> — empty if SKU does not exist

        if (stockOpt.isEmpty()) {
            // SKU not found in inventory — log and publish rejection
            logger.warn("SKU={} not found in inventory for orderId={}", sku, orderId);
            publishRejection(orderId, sku, quantity, 0,
                    "SKU not found in inventory: " + sku);
            return;
        }

        int rowsUpdated = stockRepository.decrementStock(sku, quantity);
        // atomic check-and-decrement — returns 1 if success, 0 if insufficient stock

        if (rowsUpdated == 0) {
            Integer availableQuantity = stockOpt.get().getQuantity();
            // get current stock from the Optional we already fetched above
            logger.warn("[OUT OF STOCK] sku={} requested={} available={} orderId={}",
                    sku, quantity, availableQuantity, orderId);
            publishRejection(orderId, sku, quantity, availableQuantity,
                    "Insufficient stock: requested " + quantity +
                    " but only " + availableQuantity + " available");
        } else {
            logger.info("[RESERVED] sku={} qty={} orderId={}", sku, quantity, orderId);
        }
    }

    private void publishRejection(Long orderId, String sku,
                                  int requested, int available, String reason) {
        try {
            OrderRejectedEvent event = new OrderRejectedEvent(
                    orderId, sku, requested, available, reason,
                    LocalDateTime.now().toString()
            );
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("orders.rejected", orderId.toString(), json);
            // orderId as key — keeps all events for one order on same partition
            logger.info("[ORDER_REJECTED] Published for orderId={} sku={}", orderId, sku);
        } catch (JsonProcessingException e) {
            logger.error("[ORDER_REJECTED] Failed to serialize event for orderId={}", orderId, e);
        }
    }
}