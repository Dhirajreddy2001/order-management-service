package com.example.inventory_service.event;

public class OrderRejectedEvent {

    private Long orderId;
    private String sku;
    private Integer requestedQuantity;
    private Integer availableQuantity;

    private String reason;

    private String occurredAt;

    public OrderRejectedEvent() {
        // default constructor for deserialization
    }

    public OrderRejectedEvent(Long orderId, String sku, Integer requestedQuantity, Integer availableQuantity,
            String reason, String occuredAt) {
        this.orderId = orderId;
        this.sku = sku;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.reason = reason;
        this.occurredAt = occuredAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(String occurredAt) {
        this.occurredAt = occurredAt;
    }

}
