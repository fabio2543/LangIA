package com.langia.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuração do RabbitMQ para processamento assíncrono de geração de trilhas.
 */
@Configuration
public class RabbitMQConfig {

    // ========== CONSTANTES ==========

    // Exchange principal
    public static final String TRAIL_EXCHANGE = "trail.exchange";

    // Filas de geração
    public static final String TRAIL_GENERATION_QUEUE = "trail.generation.queue";
    public static final String TRAIL_GENERATION_DLQ = "trail.generation.dlq";

    // Fila de notificações
    public static final String TRAIL_NOTIFICATION_QUEUE = "trail.notification.queue";

    // Routing keys
    public static final String TRAIL_GENERATION_ROUTING_KEY = "trail.generation";
    public static final String TRAIL_NOTIFICATION_ROUTING_KEY = "trail.notification";
    public static final String TRAIL_DLQ_ROUTING_KEY = "trail.generation.dlq";

    // ========== EXCHANGE ==========

    @Bean
    DirectExchange trailExchange() {
        return new DirectExchange(TRAIL_EXCHANGE, true, false);
    }

    // ========== FILAS ==========

    @Bean
    Queue trailGenerationQueue() {
        return QueueBuilder.durable(TRAIL_GENERATION_QUEUE)
                .withArgument("x-dead-letter-exchange", TRAIL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", TRAIL_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 300000) // 5 minutos TTL
                .build();
    }

    @Bean
    Queue trailGenerationDlq() {
        return QueueBuilder.durable(TRAIL_GENERATION_DLQ)
                .withArgument("x-message-ttl", 86400000) // 24 horas TTL
                .build();
    }

    @Bean
    Queue trailNotificationQueue() {
        return QueueBuilder.durable(TRAIL_NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", 60000) // 1 minuto TTL
                .build();
    }

    // ========== BINDINGS ==========

    @Bean
    Binding trailGenerationBinding(Queue trailGenerationQueue, DirectExchange trailExchange) {
        return BindingBuilder.bind(trailGenerationQueue)
                .to(trailExchange)
                .with(TRAIL_GENERATION_ROUTING_KEY);
    }

    @Bean
    Binding trailDlqBinding(Queue trailGenerationDlq, DirectExchange trailExchange) {
        return BindingBuilder.bind(trailGenerationDlq)
                .to(trailExchange)
                .with(TRAIL_DLQ_ROUTING_KEY);
    }

    @Bean
    Binding trailNotificationBinding(Queue trailNotificationQueue, DirectExchange trailExchange) {
        return BindingBuilder.bind(trailNotificationQueue)
                .to(trailExchange)
                .with(TRAIL_NOTIFICATION_ROUTING_KEY);
    }

    // ========== CONVERTERS ==========

    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        template.setExchange(TRAIL_EXCHANGE);
        return template;
    }
}
