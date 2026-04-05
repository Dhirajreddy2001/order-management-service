package com.ordermanagement.invoice_woker.message;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceJobMessage {

    private Long orderid;
    private String customerId;
    private BigDecimal totalAmount;

    private String status;

    private LocalDateTime enqueuedAt;

    public InvoiceJobMessage() {
    }

    public InvoiceJobMessage(Long orderid, String customerId, BigDecimal totalAmount, String status,
            LocalDateTime enqueuedAt) {
        this.orderid = orderid;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.enqueuedAt = enqueuedAt;
    }

    public Long getOrderid() {
        return orderid;
    }

    public void setOrderid(Long orderid) {
        this.orderid = orderid;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEnqueuedAt() {
        return enqueuedAt;
    }

    public void setEnqueuedAt(LocalDateTime enqueuedAt) {
        this.enqueuedAt = enqueuedAt;
    }
}
