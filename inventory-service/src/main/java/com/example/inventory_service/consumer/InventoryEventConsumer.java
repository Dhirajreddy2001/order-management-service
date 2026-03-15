package com.example.inventory_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.inventory_service.event.OrderCreatedEvent;
import com.example.inventory_service.service.StockService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class InventoryEventConsumer {

        private static final Logger logger = LoggerFactory.getLogger(InventoryEventConsumer.class);

        private final ObjectMapper objectMapper = new ObjectMapper()
                        .registerModule(new JavaTimeModule());
        // JavaTimeModule — teaches Jackson how to handle LocalDateTime fields

        private final StockService stockService;

        public InventoryEventConsumer(StockService stockService) {
                this.stockService = stockService;
                // constructor injection — Spring auto-wires StockService bean
        }

        @RetryableTopic(attempts = "4",
                        // total = 1 original attempt + 3 retries before giving up
                        backOff = @BackOff(delay = 1000, multiplier = 2),
                        // retry 1: wait 1s, retry 2: wait 2s, retry 3: wait 4s
                        // exponential backoff — gives failing system time to recover
                        dltTopicSuffix = ".DLT",
                        // dead letter topic = orders.created.DLT

                        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,

                        sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.MULTIPLE_TOPICS,
                        // retry topic names: orders.created-0, orders.created-1, orders.created-2
                        exclude = { JsonProcessingException.class, NullPointerException.class }

        )
        @KafkaListener(topics = "orders.created", groupId = "inventory-service-group")
        public void handleOrderCreatedEvent(String message) throws JsonProcessingException {
                // throws declaration — allows JsonProcessingException to propagate
                // @RetryableTopic needs exceptions to propagate to trigger retry/DLT routing
                // if you catch and swallow — Spring commits offset and never retries

                OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
                // JsonProcessingException propagates here if JSON is malformed
                // excluded from retry — goes straight to DLT

                logger.info("[inventory-service] Received ORDER_CREATED: orderId={}",
                                event.getOrderId());

                if (event.getItems() == null || event.getItems().isEmpty()) {
                        logger.warn("Order {} has no items — skipping", event.getOrderId());
                        return;
                        // returning normally — offset committed — consumer moves on
                }

                event.getItems().forEach(item -> stockService.reserveStock(
                                item.getSku(), // which product to decrement
                                item.getQuantity(), // how many units
                                event.getOrderId() // for logging and tracing
                ));
                // if stockService throws DataAccessException (transient) — retry kicks in
                // if stockService throws NullPointerException (permanent) — straight to DLT

                logger.info("[inventory-service] Done: orderId={} — {} items processed",
                                event.getOrderId(), event.getItems().size());
        }

        @DltHandler
        // separate method at class level — called when all retries are exhausted
        // Spring Kafka routes the failed message here after final retry failure
        public void handleDlt(
                        String message,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        // which topic the message came from
                        @Header("kafka_exception-fqcn") String exceptionClass,
                        // fully qualified exception class name — tells you failure type
                        @Header("kafka_exception-message") String exceptionMessage,
                        // the exception message — tells you WHY it failed
                        @Header("kafka_original-offset") long originalOffset,
                        // position in the original topic — use to find the exact message
                        @Header("kafka_original-partition") int originalPartition) {
                // which partition the original message came from
                logger.error("[DLT] Exhausted retries. topic={} offset={} partition={} exception={} error={}",
                                topic, originalOffset, originalPartition, exceptionClass, exceptionMessage);
                // Day 10: persist to dead_letter_log table for operator review
                // Day 10: send Slack/PagerDuty alert
        }
}