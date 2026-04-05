package com.ordermanagement.order_api.producer;

import com.ordermanagement.order_api.event.OrderCreatedEvent;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;


@Component
public class OrderEventProducer {

    public static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);

    private static final String TOPIC = "orders.created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        String messageKey = event.getOrderId().toString();
        CompletableFuture<SendResult<String, OrderCreatedEvent>> future = kafkaTemplate.send(TOPIC, messageKey, event);

        future.whenComplete((result ,  ex) -> {
            if( ex != null){
                logger.error("Failed to publish event: {}", ex.getMessage());
            } else {
                logger.info("Event published successfully: topic={}, partition={}, offset={}",
                
                event.getOrderId(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
            }
        });

    }


    
    
}
