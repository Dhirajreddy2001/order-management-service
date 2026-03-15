package com.example.inventory_service.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import com.example.inventory_service.entity.StockEntity;
import com.example.inventory_service.repository.StockRepository;

@Component
public class DataSeeder implements ApplicationRunner {

    private final StockRepository stockRepository;

    public DataSeeder(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Seed initial stock data
        if (stockRepository.count() == 0) {
            stockRepository.save(new StockEntity("SKU-001", "Google Pixel 9", 100));
            stockRepository.save(new StockEntity("SKU-002", "Apple iPhone 17", 150));
            stockRepository.save(new StockEntity("SKU-003", "Samsung Galaxy S23", 120));
            stockRepository.save(new StockEntity("SKU-004", "OnePlus 9 Pro", 80));
            stockRepository.save(new StockEntity("SKU-006", "Google Pixel 9 Pro", 90));
            stockRepository.save(new StockEntity("SKU-007", "Apple iPhone 17 Pro Max", 200));
            stockRepository.save(new StockEntity("SKU-008", "Samsung Galaxy Note 25", 70));

            System.out.println("Seeded initial stock data");
        }
    }

}
