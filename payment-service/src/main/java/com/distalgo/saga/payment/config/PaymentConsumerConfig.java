package com.distalgo.saga.payment.config;

import com.distalgo.saga.events.InventoryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

/**
 * Payment consumes Inventory Event from inventory-event topic
 */
@Configuration
public class PaymentConsumerConfig {
    @Bean
    public ReceiverOptions<String, InventoryEvent> kafkaReceiver(@Value(value = "inventory-event") String topic, KafkaProperties kafkaProperties) {
        ReceiverOptions<String, InventoryEvent> basicOptions = ReceiverOptions.create(kafkaProperties.buildConsumerProperties(null));
        return basicOptions.subscription(Collections.singletonList(topic));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, InventoryEvent> inventoryEventConsumerKafkaTemplate(ReceiverOptions<String, InventoryEvent> receiverOptions) {
        return new ReactiveKafkaConsumerTemplate<String, InventoryEvent>(receiverOptions);
    }
}
