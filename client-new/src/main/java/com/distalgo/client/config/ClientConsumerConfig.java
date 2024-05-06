package com.distalgo.client.config;

import com.distalgo.saga.events.CallbackEvent;
import com.distalgo.saga.events.InventoryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

/**
 * Client consumes from the callback-event topic
 */
@Configuration
public class ClientConsumerConfig {
    @Bean
    public ReceiverOptions<String, CallbackEvent> kafkaReceiverCallback(@Value(value = "callback-event") String topic, KafkaProperties kafkaProperties) {
        ReceiverOptions<String, CallbackEvent> basicOptions = ReceiverOptions.<String, CallbackEvent>create(kafkaProperties.buildConsumerProperties(null));
        return basicOptions.subscription(Collections.singletonList(topic));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, CallbackEvent> callbackEventConsumerKafkaTemplate(ReceiverOptions<String, CallbackEvent> receiverOptions) {
        return new ReactiveKafkaConsumerTemplate<String, CallbackEvent>(receiverOptions);
    }
}
