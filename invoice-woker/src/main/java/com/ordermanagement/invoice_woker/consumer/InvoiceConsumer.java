package com.ordermanagement.invoice_woker.consumer;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ordermanagement.invoice_woker.message.InvoiceJobMessage;

@Component
@Profile("rabbit")
public class InvoiceConsumer {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceConsumer.class);

    @RabbitListener(queues = "invoice.jobs")
    public void handleInvoiceJob(InvoiceJobMessage message) {
        logger.info("Received invoice job message: {}", message);

        try {

            generateInvoice(message);
            logger.info("Invoice generated successfully for orderId: {}", message.getOrderid());
        } catch (Exception e) {
            logger.error("Error processing invoice job for orderId: {}. Error: {}", message.getOrderid(), e.getMessage());

            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "invoice.jobs.dlq")
    public void handleFailedInvoiceJob(InvoiceJobMessage message) {
        logger.error("Received message in DLQ for orderId: {}. Message: {}", message.getOrderid(), message);

    }

    private void generateInvoice(InvoiceJobMessage message) throws Exception {

        logger.info("Generating invoice for orderId: {}, customerId: {}, totalAmount: {}",
                message.getOrderid(), message.getCustomerId(), message.getTotalAmount());

        // Simulate potential failure for demonstration
        if (message.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Invalid total amount for orderId: " + message.getOrderid());
        }

        // Simulate processing time
        Thread.sleep(2000);
    }
}
