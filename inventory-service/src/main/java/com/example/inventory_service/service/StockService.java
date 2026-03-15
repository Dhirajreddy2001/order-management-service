package com.example.inventory_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.inventory_service.repository.StockRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional

    public void reserveStock(String sku, int quantity, Long orderId) {
        logger.info("Reserving stock for SKU: {}, Quantity: {}- orderId: {}", sku, quantity, orderId);

        boolean skuExists = stockRepository.findBySku(sku).isPresent();

        if (!skuExists) {
            logger.warn("SKU: {} not found in inventory for orderId: {}", sku, orderId);
            return;
        }

        int rowsUpdated = stockRepository.decreaseStock(sku, quantity);
        if (rowsUpdated == 0) {
            logger.warn("Failed to reserve stock for SKU: {}. Not enough quantity available for orderId: {}", sku,
                    orderId);
        } else {
            logger.info("Successfully reserved stock for SKU: {}, Quantity: {} for orderId: {}", sku, quantity,
                    orderId);
        }

    }

}
