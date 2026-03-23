package com.example.inventory_service;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;

@TestConfiguration
public class KafkaTestConfig {

    @Bean(name = "kafkaAdmin")
    KafkaAdmin kafkaAdmin() {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        KafkaAdmin kafkaAdmin = new KafkaAdmin(config);
        kafkaAdmin.setFatalIfBrokerNotAvailable(false);
        kafkaAdmin.setAutoCreate(false);
        return kafkaAdmin;
    }
}
