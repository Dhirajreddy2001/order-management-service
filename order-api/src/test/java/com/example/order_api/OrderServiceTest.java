package com.example.order_api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.order_api.dto.OrderItemDTO;
import com.example.order_api.dto.OrderRequestDTO;
import com.example.order_api.dto.OrderResponseDTO;
import com.example.order_api.entity.OrderEntity;
import com.example.order_api.exception.OrderNotFoundException;
import com.example.order_api.repository.OrderRepository;
import com.example.order_api.service.OrderService;

@ExtendWith(MockitoExtension.class)
// tells JUnit to activate Mockito — processes @Mock annotations
// no Spring context needed — pure unit test, very fast
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    // Mockito creates a fake OrderRepository
    // all methods return empty/null by default unless configured with when()

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // inject the mock repository into the service via constructor
        // this is why constructor injection is better than field injection
        // — you can pass mocks without Spring at all
        orderService = new OrderService(orderRepository);
    }

    @Test
    void createOrder_ShouldReturnOrderTotals() {
        // mock repo.save() — return an entity with the same data back
        // without this mock, save() returns null and mapToDTO() throws NPE
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> {
                    OrderEntity entity = invocation.getArgument(0);
                    entity.setId(1L);
                    // simulate DB assigning the id
                    return entity;
                });

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
    void createOrder_ShouldReturnCorrectOrderId() {
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> {
                    OrderEntity entity = invocation.getArgument(0);
                    entity.setId(1L);
                    return entity;
                });

        OrderRequestDTO request = buildSimpleRequest("C123");
        OrderResponseDTO response = orderService.createOrder(request);

        assertNotNull(response.getOrderId());
    }

    @Test
    void createOrder_ShouldSetStatusToPending() {
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> {
                    OrderEntity entity = invocation.getArgument(0);
                    entity.setId(1L);
                    return entity;
                });

        OrderRequestDTO request = buildSimpleRequest("C123");
        OrderResponseDTO response = orderService.createOrder(request);

        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void createOrder_shouldReturnCorrectCustomerId() {
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> {
                    OrderEntity entity = invocation.getArgument(0);
                    entity.setId(1L);
                    return entity;
                });

        OrderRequestDTO request = buildSimpleRequest("C123");
        OrderResponseDTO response = orderService.createOrder(request);

        assertEquals("C123", response.getCustomerId());
    }

    @Test
    void createOrder_shouldReturnOrderAfterCreation() {
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> {
                    OrderEntity entity = invocation.getArgument(0);
                    entity.setId(1L);
                    return entity;
                });

        OrderRequestDTO request = buildSimpleRequest("C123");
        OrderResponseDTO response = orderService.createOrder(request);

        assertNotNull(response);
    }

    @Test
    void createOrder_shouldReturnCorrectLineTotalPerItem() {
        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> {
                    OrderEntity entity = invocation.getArgument(0);
                    entity.setId(1L);
                    return entity;
                });

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
        // build a fake saved entity to return from findById
        OrderEntity savedEntity = new OrderEntity();
        savedEntity.setId(1L);
        savedEntity.setCustomerId("C123");
        savedEntity.setTotalAmount(new BigDecimal("99.98"));
        savedEntity.setStatus("PENDING");
        savedEntity.setItems(List.of());
        // empty items list — we are testing the lookup not the items

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(savedEntity));
        // mock findById to return our fake entity

        OrderResponseDTO found = orderService.getOrderById(1L);

        assertEquals(1L, found.getOrderId());
        assertEquals("C123", found.getCustomerId());
    }

    @Test
    void getOrderById_shouldThrowExceptionForUnknownId() {
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());
        // mock returns empty — triggers orElseThrow in service

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });
    }

    // helper — builds a simple one-item request
    private OrderRequestDTO buildSimpleRequest(String customerId) {
        OrderItemDTO item = new OrderItemDTO();
        item.setSku("SKU123");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("49.99"));

        OrderRequestDTO request = new OrderRequestDTO();
        request.setCustomerId(customerId);
        request.setItems(List.of(item));

        return request;
    }
}