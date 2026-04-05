package com.example.invoice_woker.consumer;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.invoice_woker.message.InvoiceJobMessage;

import io.awspring.cloud.sqs.annotation.SqsListener;

@Component
@Profile("sqs")
public class SQSInvoiceConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SQSInvoiceConsumer.class);

    @SqsListener("invoice-jobs")
    public void handleInvoiceJobSqs(InvoiceJobMessage message) {
        logger.info("Received invoice job message from SQS: {}", message);

        try {
            generateInvoice(message);
            logger.info("Invoice generated successfully for orderId: {}", message.getOrderid());
        } catch (Exception e) {
            logger.error("Error processing invoice job for orderId: {}. Error: {}", message.getOrderid(), e.getMessage());
            throw new RuntimeException("Failed to process invoice job for orderId: " + message.getOrderid(), e);
        }
    }

    private void generateInvoice(InvoiceJobMessage message) throws InterruptedException {
        logger.info("Generating invoice for orderId: {}, customerId: {}, totalAmount: {}",
                message.getOrderid(), message.getCustomerId(), message.getTotalAmount());

        if (message.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Total amount must be greater than zero for orderId: " + message.getOrderid());
        }
        Thread.sleep(2000); // Simulate time-consuming task
    }
}
