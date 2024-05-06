package com.distalgo.saga.order.config;

import com.distalgo.saga.events.CallbackEvent;
import com.distalgo.saga.events.OrderEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Configuration
public class OrderEventPublisherConfig {
    @Bean
    public ReactiveKafkaProducerTemplate<String, OrderEvent> orderEventProducerKafkaTemplate(KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties(null);
        return new ReactiveKafkaProducerTemplate<String, OrderEvent>(SenderOptions.create(props));
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, CallbackEvent> callbackEventProducerKafkaTemplate(KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties(null);
        return new ReactiveKafkaProducerTemplate<String, CallbackEvent>(SenderOptions.create(props));
    }
}
