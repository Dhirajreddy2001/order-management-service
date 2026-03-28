package com.example.order_api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order_api.dto.OrderItemResponseDTO;
import com.example.order_api.dto.OrderRequestDTO;
import com.example.order_api.dto.OrderResponseDTO;
import com.example.order_api.entity.OrderEntity;
import com.example.order_api.entity.OrderItemEntity;
import com.example.order_api.event.OrderCreatedEvent;
import com.example.order_api.exception.OrderNotFoundException;
import com.example.order_api.message.InvoiceJobMessage;
import com.example.order_api.producer.InvoiceJobProducer;
import com.example.order_api.producer.OrderEventProducer;
import com.example.order_api.repository.OrderRepository;

@Service
public class OrderService {
    // Implement the business logic for creating and retrieving orders here

    private final OrderRepository orderRepository;

    private final OrderEventProducer orderEventProducer; //kafka producer for order events

    private final InvoiceJobProducer invoiceJobProducer; //MQ producer for invoice generation jobs

    public OrderService(OrderRepository orderRepository, OrderEventProducer orderEventProducer, InvoiceJobProducer invoiceJobProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
        this.invoiceJobProducer = invoiceJobProducer;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        // For demonstration, we will just create a simple order response with a
        // generated ID

        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setCustomerId(request.getCustomerId());
        orderEntity.setTotalAmount(totalAmount);
        orderEntity.setStatus("PENDING");

        List<OrderItemEntity> orderItems = request.getItems().stream()
                .map(item -> {
                    OrderItemEntity orderItem = new OrderItemEntity();
                    orderItem.setSku(item.getSku());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setUnitPrice(item.getUnitPrice());
                    orderItem.setOrder(orderEntity); // Set the relationship

                    return orderItem;
                })
                .collect(Collectors.toList());

        orderEntity.setItems(orderItems); // Set the items to the order entity

        OrderEntity savedOrder = orderRepository.save(orderEntity);

        List<OrderCreatedEvent.OrderItemEvent> eventItems = savedOrder.getItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItemEvent(item.getSku(), item.getQuantity()))
                .collect(Collectors.toList());

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                "ORDER_CREATED",
                "1.0",
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getTotalAmount(),
                eventItems,
                LocalDateTime.now());

        orderEventProducer.publishOrderCreatedEvent(event);

        invoiceJobProducer.enqueueInvoiceJob(
                new InvoiceJobMessage(
                        savedOrder.getId(),
                        savedOrder.getCustomerId(),
                        savedOrder.getTotalAmount(),
                        savedOrder.getStatus(),
                        LocalDateTime.now()
                )
        );

        return mapToDTO(savedOrder);

    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId) {

        return orderRepository.findById(orderId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    public java.util.List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private OrderResponseDTO mapToDTO(OrderEntity orderEntity) {
        List<OrderItemResponseDTO> items = orderEntity.getItems().stream()
                .map(item -> {
                    OrderItemResponseDTO responseItem = new OrderItemResponseDTO();
                    responseItem.setItemId(item.getId());
                    responseItem.setSku(item.getSku());
                    responseItem.setQuantity(item.getQuantity());
                    responseItem.setUnitPrice(item.getUnitPrice());
                    responseItem.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    return responseItem;
                })
                .collect(Collectors.toList());

        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(orderEntity.getId());
        response.setCustomerId(orderEntity.getCustomerId());
        response.setItems(items);
        response.setTotalAmount(orderEntity.getTotalAmount());
        response.setStatus(orderEntity.getStatus());
        response.setCreatedAt(orderEntity.getCreatedAt());
        response.setUpdatedAt(orderEntity.getUpdatedAt());
        return response;
    }

}
