package com.example.order_api.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import tools.jackson.databind.json.JsonMapper;

@Configuration
@Profile("rabbit")
public class RabbitMQConfig {

    public static final String INVOICE_QUEUE = "invoice.jobs";
    public static final String INVOICE_DLQ = "invoice.jobs.dlq";
    public static final String INVOICE_EXCHANGE = "invoice.exchange";
    public static final String INVOICE_ROUTING_KEY = "invoice.routingkey";

    @Bean
    public Queue invoiceQueue() {

        return QueueBuilder.durable(INVOICE_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", INVOICE_DLQ)
                .build();
    }

    @Bean
    public Queue invoiceDLQ() {

        return QueueBuilder.durable(INVOICE_DLQ).build();
    }

    @Bean
    public DirectExchange invoiceExchange() {
        return new DirectExchange(INVOICE_EXCHANGE);
    }

    @Bean
    public Binding invoiceBinding(Queue invoiceQueue, DirectExchange invoiceExchange) {

        return BindingBuilder
                .bind(invoiceQueue)
                .to(invoiceExchange)
                .with(INVOICE_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter(JsonMapper.builder().build());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
