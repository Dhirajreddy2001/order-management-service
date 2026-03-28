package com.example.order_api.producer;

import com.example.order_api.config.RabbitMQConfig;
import com.example.order_api.message.InvoiceJobMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class InvoiceJobProducer {

    private static final Logger log = LoggerFactory.getLogger(InvoiceJobProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public InvoiceJobProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enqueueInvoiceJob(InvoiceJobMessage message) {
        log.info("Publishing invoice job: exchange={}, routingKey={}, orderId={}",
                RabbitMQConfig.INVOICE_EXCHANGE,
                RabbitMQConfig.INVOICE_ROUTING_KEY,
                message.getOrderid());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVOICE_EXCHANGE,
                    RabbitMQConfig.INVOICE_ROUTING_KEY,
                    message);

            log.info("Invoice job published successfully for orderId={}", message.getOrderid());
        } catch (Exception ex) {
            log.error("Failed to publish invoice job for orderId={}: {}", message.getOrderid(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
