package com.ordermanagement.order_api.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.ordermanagement.order_api.message.InvoiceJobMessage;

import io.awspring.cloud.sqs.operations.SqsTemplate;

@Component
@Profile("sqs")
public class InvoiceSqsJobProducer implements InvoiceJobPublisher {

    private static final Logger log = LoggerFactory.getLogger(InvoiceSqsJobProducer.class);

    private final SqsTemplate sqsTemplate;

    public InvoiceSqsJobProducer(SqsTemplate sqsTemplate) {
        this.sqsTemplate = sqsTemplate;
    }

    @Override
    public void enqueueInvoiceJob(InvoiceJobMessage message) {
        log.info("Publishing invoice job to SQS for orderId={}", message.getOrderid());

        try {
            sqsTemplate.send(options -> options.queue("invoice-jobs").payload(message));
            log.info("Invoice job published successfully to SQS for orderId={}", message.getOrderid());
        } catch (Exception ex) {
            log.error("Failed to publish invoice job to SQS for orderId={}: {}", message.getOrderid(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
