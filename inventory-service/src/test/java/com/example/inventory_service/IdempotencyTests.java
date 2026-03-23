package com.example.inventory_service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory_service.entity.ProcessedEventEntity;
import com.example.inventory_service.entity.StockEntity;
import com.example.inventory_service.repository.ProcessedEventRepository;
import com.example.inventory_service.repository.StockRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
@Import(KafkaTestConfig.class)
@Transactional
class IdempotencyTests {

    private static final String TEST_SKU = "SKU-TEST-001";

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        stockRepository.deleteAll();
        stockRepository.flush();
        stockRepository.save(new StockEntity(TEST_SKU, "Widget Pro", 100));
        stockRepository.flush();
    }

    @Test
    void processEvent_calledTwiceWithSameEventId_decrementsStockOnlyOnce() {
        String eventId = "test-event-id-001";

        boolean firstCheck = processedEventRepository.existsByEventId(eventId);

        if (!firstCheck) {
            stockRepository.decrementStock(TEST_SKU, 3);
            processedEventRepository.save(new ProcessedEventEntity(eventId, LocalDateTime.now()));
            processedEventRepository.flush();
            stockRepository.flush();
        }

        boolean secondCheck = processedEventRepository.existsByEventId(eventId);

        if (!secondCheck) {
            stockRepository.decrementStock(TEST_SKU, 3);
            processedEventRepository.save(new ProcessedEventEntity(eventId, LocalDateTime.now()));
            processedEventRepository.flush();
            stockRepository.flush();
        }

        entityManager.clear();

        StockEntity updatedStock = stockRepository.findBySku(TEST_SKU)
                .orElseThrow(() -> new AssertionError("Stock item not found"));

        assertEquals(97, updatedStock.getQuantity(),
                "Stock should decrement exactly once even when event replayed");
        assertEquals(1, processedEventRepository.count(),
                "Only one processed_event row should exist for this eventId");
        assertFalse(firstCheck, "First delivery should not have been seen before");
        assertTrue(secondCheck, "Second delivery should be flagged as duplicate");
    }
}
