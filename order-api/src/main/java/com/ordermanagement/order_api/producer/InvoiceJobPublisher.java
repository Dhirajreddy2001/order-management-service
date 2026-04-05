package com.ordermanagement.order_api.producer;

import com.ordermanagement.order_api.message.InvoiceJobMessage;

public interface InvoiceJobPublisher {
    void enqueueInvoiceJob(InvoiceJobMessage message);
}
