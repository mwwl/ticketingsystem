package com.distalgo.saga.inventory.config;

import com.distalgo.saga.events.OrderEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

@Configuration
public class InventoryConsumerConfig {
    @Bean
    public ReceiverOptions<String, OrderEvent> kafkaReceiver(@Value(value = "order-event") String topic, KafkaProperties kafkaProperties) {
        ReceiverOptions<String, OrderEvent> basicOptions = ReceiverOptions.create(kafkaProperties.buildConsumerProperties(null));
        return basicOptions.subscription(Collections.singletonList(topic));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, OrderEvent> orderEventConsumerKafkaTemplate(ReceiverOptions<String, OrderEvent> receiverOptions) {
        return new ReactiveKafkaConsumerTemplate<String, OrderEvent>(receiverOptions);
    }
}