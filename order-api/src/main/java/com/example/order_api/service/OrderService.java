package com.example.order_api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.example.order_api.dto.OrderItemDTO;
import com.example.order_api.dto.OrderItemResponseDTO;
import com.example.order_api.dto.OrderRequestDTO;
import com.example.order_api.dto.OrderResponseDTO;
import com.example.order_api.exception.OrderNotFoundException;

@Service
public class OrderService {
    // Implement the business logic for creating and retrieving orders here

    private final Map<Long, OrderResponseDTO> orderStore = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        // For demonstration, we will just create a simple order response with a generated ID
        List<OrderItemResponseDTO> responseItems = new ArrayList<>();
        AtomicLong itemCounter = new AtomicLong(1);

        for (OrderItemDTO item : request.getItems()){
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderItemResponseDTO responseItem = new OrderItemResponseDTO();
            responseItem.setItemId(itemCounter.getAndIncrement());
            responseItem.setSku(item.getSku());
            responseItem.setQuantity(item.getQuantity());
            responseItem.setUnitPrice(item.getUnitPrice());
            responseItem.setLineTotal(lineTotal);
            
            responseItems.add(responseItem);
        }

        BigDecimal totalAmount = responseItems.stream()
                .map(OrderItemResponseDTO::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(idCounter.getAndIncrement());
        response.setCustomerId(request.getCustomerId());
        response.setStatus("PENDING");
        response.setTotalAmount(totalAmount);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        response.setItems(responseItems);

        orderStore.put(response.getOrderId(), response);
        return response;
    }

    public OrderResponseDTO getOrderById(Long orderId) {
       
        OrderResponseDTO order = orderStore.get(orderId);

        if (order == null) {
            throw new OrderNotFoundException(orderId); // Or throw an exception if you prefer
        }

        return order;
    }

    public java.util.List<OrderResponseDTO> getAllOrders() {
        return new ArrayList<>(orderStore.values());
    }

}
