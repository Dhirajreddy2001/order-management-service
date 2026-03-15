package com.example.inventory_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.inventory_service.entity.StockEntity;

public interface StockRepository extends JpaRepository<StockEntity, Long> {

    Optional<StockEntity> findBySku(String sku);
    // derived query — SELECT * FROM stock_items WHERE sku = ?

    @Modifying
    @Query("UPDATE StockEntity s SET s.quantity = s.quantity - :quantity WHERE s.sku = :sku AND s.quantity >= :quantity")
    // atomic check-and-decrement — only updates if enough stock exists
    // returns 1 = success, 0 = insufficient stock
    int decrementStock(@Param("sku") String sku, @Param("quantity") int quantity);
}