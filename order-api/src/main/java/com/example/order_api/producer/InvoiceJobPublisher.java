package com.example.order_api.producer;

import com.example.order_api.message.InvoiceJobMessage;

public interface InvoiceJobPublisher {
    void enqueueInvoiceJob(InvoiceJobMessage message);
}
