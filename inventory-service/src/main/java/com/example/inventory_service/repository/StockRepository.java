package com.example.inventory_service.repository;

import com.example.inventory_service.entity.StockEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface StockRepository extends JpaRepository<StockEntity, Long> {

    Optional<StockEntity> findBySku(String sku);

    @Modifying
    @Query("UPDATE StockEntity s SET s.quantity = s.quantity - :quantity WHERE s.sku = :sku AND s.quantity >= :quantity")

    int decreaseStock(@Param("sku") String sku, @Param("quantity") int quantity);

}
