package com.example.inventory_service;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
@Import(KafkaTestConfig.class)
class InventoryServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
