package com.example.inventory_service.repository;

import com.example.inventory_service.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository  extends JpaRepository<ProcessedEventEntity, Long> {
    boolean existsByEventId(String eventId);

}
