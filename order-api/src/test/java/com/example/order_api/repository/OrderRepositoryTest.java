package com.example.order_api.repository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.order_api.entity.OrderEntity;
import com.example.order_api.entity.OrderItemEntity;

@DataJpaTest
// loads only JPA layer + H2 auto-configured
// each test auto-rolls back after — tests are fully isolated
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void saveOrder_ShouldPersistData() {

        // ── ARRANGE ──────────────────────────────────────────
        OrderItemEntity item = new OrderItemEntity();
        item.setSku("TEST-SKU-001");
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(10.00));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setSku("TEST-SKU-002");
        item2.setQuantity(1);
        item2.setUnitPrice(BigDecimal.valueOf(20.00));

        OrderEntity order = new OrderEntity();
        order.setCustomerId("C1");
        order.setTotalAmount(BigDecimal.valueOf(40.00));
        order.setStatus("PENDING");

        // link items to order — sets FK order_items.order_id = orders.id
        // without setOrder() the FK column is null and save() fails
        item.setOrder(order);
        item2.setOrder(order);
        order.setItems(List.of(item, item2));

        // ── ACT ──────────────────────────────────────────────
        OrderEntity savedOrder = orderRepository.save(order);
        // CascadeType.ALL saves both items automatically
        // @PrePersist fires here — sets createdAt and updatedAt

        // ── ASSERT ───────────────────────────────────────────
        assertNotNull(savedOrder.getId());
        assertNotNull(savedOrder.getCreatedAt());
        assertNotNull(savedOrder.getUpdatedAt());
        assertEquals("C1", savedOrder.getCustomerId());
        assertEquals(BigDecimal.valueOf(40.00), savedOrder.getTotalAmount());
        assertEquals(2, savedOrder.getItems().size());
        assertEquals("TEST-SKU-001", savedOrder.getItems().get(0).getSku());
        assertEquals("TEST-SKU-002", savedOrder.getItems().get(1).getSku());
    }

    @Test
    void saveOrder_withException_rollsBackCompletely() {

        // ── ARRANGE ──────────────────────────────────────────
        long countBefore = orderRepository.count();
        // snapshot how many orders exist before this test runs

        // ── ACT ──────────────────────────────────────────────
        OrderEntity order = new OrderEntity();
        order.setCustomerId("C2");
        order.setTotalAmount(BigDecimal.valueOf(50.00));
        order.setStatus("PENDING");
        orderRepository.save(order);
        // order written to DB but not yet committed

        long countAfterSave = orderRepository.count();
        assertEquals(countBefore + 1, countAfterSave);
        // verify order was saved — count increased by 1
        // @DataJpaTest rolls back everything after this test completes
        // so the next test starts with a clean slate
    }

}