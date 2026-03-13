package com.example.order_api.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.order_api.exception.OrderNotFoundException;
import com.example.order_api.service.OrderService;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
// ↑ replace the existing @SpringBootTest — overrides localhost:9092 with
// embedded broker
@EmbeddedKafka(partitions = 1, topics = { "orders.created" })
@AutoConfigureMockMvc
public class OrderControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private OrderService orderService;

        @Test
        // httpBasic("admin", "secret") sends real credentials in the request header
        // more realistic than @WithMockUser — tests the actual auth mechanism
        void createOrder_missingCustomerId_returns400() throws Exception {
                mockMvc.perform(
                                post("/orders")
                                                .with(httpBasic("admin", "secret"))
                                                // ↑ sends Authorization: Basic YWRtaW46c2VjcmV0 header
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {
                                                                    "items": [
                                                                        {
                                                                            "sku": "TEST-SKU-001",
                                                                            "quantity": 2,
                                                                            "unitPrice": 10.00
                                                                        }
                                                                    ]
                                                                }
                                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").exists())
                                .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        // authenticated with real credentials — tests empty items validation
        void createOrder_emptyItems_returns400() throws Exception {
                mockMvc.perform(
                                post("/orders")
                                                .with(httpBasic("admin", "secret"))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("""
                                                                {"customerId": "C1", "items": []}
                                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        // authenticated — mock throws OrderNotFoundException → 404
        void getOrder_unknownId_returns404() throws Exception {
                when(orderService.getOrderById(999L))
                                .thenThrow(new OrderNotFoundException(999L));

                mockMvc.perform(get("/orders/999")
                                .with(httpBasic("admin", "secret")))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        // authenticated — bad input must always be 400 never 500
        void createOrder_badInput_neverReturns500() throws Exception {
                mockMvc.perform(
                                post("/orders")
                                                .with(httpBasic("admin", "secret"))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        // no credentials — proves security is enforced
        // security intercepts before controller is even called
        void createOrder_noAuth_returns401() throws Exception {
                mockMvc.perform(
                                post("/orders")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content("{}"))
                                .andExpect(status().isUnauthorized());
        }
}