package com.example.order_api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class OrderRequestDTO {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemDTO> items;


 
    public OrderRequestDTO() {
           /**
     * Default no-argument constructor required for deserialization by frameworks
     * (for example Jackson) which instantiate DTOs via reflection.
     */
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

}
