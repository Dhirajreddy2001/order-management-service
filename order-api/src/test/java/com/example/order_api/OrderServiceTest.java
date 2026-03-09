package com.example.order_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.order_api.dto.OrderItemDTO;
import com.example.order_api.dto.OrderRequestDTO;
import com.example.order_api.dto.OrderResponseDTO;
import com.example.order_api.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderServiceTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService();
    }
    // 1. Test that creating an order with multiple items calculates the total
    // amount correctly.
    // 2. Test that creating an order returns a non-null order ID.
    // 3. Test that the order status is set to "PENDING" upon creation.
    // 4. Test that the customer ID in the response matches the one in the request.
    // 5. Test that the order response contains the correct number of items after
    // creation.

    @Test
    void createOrder_ShouldReturnOrderTotals() {

        OrderItemDTO item1 = new OrderItemDTO();
        item1.setSku("SKU123");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("49.99"));

        OrderItemDTO item2 = new OrderItemDTO();
        item2.setSku("SKU456");
        item2.setQuantity(1);
        item2.setUnitPrice(new BigDecimal("19.99"));

        OrderRequestDTO request = new OrderRequestDTO();
        request.setCustomerId("CUST_23");
        request.setItems(List.of(item1, item2));

        OrderResponseDTO response = orderService.createOrder(request);

        assertEquals(new BigDecimal("119.97"), response.getTotalAmount());

    }

    @Test
    void createOrder_ShouldReturnCorrectorderId() {

        OrderRequestDTO request = buildSimpleRequest("C123");
        OrderResponseDTO response = orderService.createOrder(request);

        assertNotNull(response.getOrderId());

    }

    @Test
    void CreateOrder_ShouldSetStatusToPending() {
        OrderRequestDTO request = buildSimpleRequest("C123");
        OrderResponseDTO response = orderService.createOrder(request);

        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void createOrder_shouldReturnCorrectCustomerId() {
        OrderRequestDTO request = buildSimpleRequest("C123");
        OrderResponseDTO response = orderService.createOrder(request);

        assertEquals("C123", response.getCustomerId());
    }

    @Test
    void createOrder_shouldReturnOrderAfterCreation() {
        OrderRequestDTO request = buildSimpleRequest("C123");
        OrderResponseDTO response = orderService.createOrder(request);

        assertNotNull(response);
    }

    @Test
    void createOrder_shouldReturnCorrectLineTotalPerItem() {
        OrderItemDTO item = new OrderItemDTO();
        item.setSku("SKU123");
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("10.00"));

        OrderRequestDTO request = new OrderRequestDTO();
        request.setCustomerId("C123");
        request.setItems(List.of(item));

        OrderResponseDTO response = orderService.createOrder(request);

        // lineTotal = 3 × 10.00 = 30.00
        assertEquals(0,
                new BigDecimal("30.00").compareTo(
                        response.getItems().get(0).getLineTotal()));
    }

    @Test
    void getOrderById_shouldReturnOrderAfterCreation() {
        OrderRequestDTO request = buildSimpleRequest("C123");
        OrderResponseDTO created = orderService.createOrder(request);

        OrderResponseDTO found = orderService.getOrderById(created.getOrderId());

        assertEquals(created.getOrderId(), found.getOrderId());
        assertEquals("C123", found.getCustomerId());
    }

    @Test
    void getOrderById_shouldThrowExceptionForUnknownId() {
        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(999L);
        });
    }

    private OrderRequestDTO buildSimpleRequest(String string) {

        OrderItemDTO item1 = new OrderItemDTO();
        item1.setSku("SKU123");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("49.99"));

        OrderRequestDTO request = new OrderRequestDTO();
        request.setCustomerId(string);
        request.setItems(List.of(item1));

        return request;
    }

}
