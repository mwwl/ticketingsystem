package com.distalgo.client.config;

import com.distalgo.saga.events.ClientEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

/**
 * Use kafka messaging to publish to the order service (topic: callback-event)
 */
@Configuration
public class ClientPublisherConfig {
    @Bean
    public ReactiveKafkaProducerTemplate<String, ClientEvent> clientEventProducerKafkaTemplate(KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties(null);
        return new ReactiveKafkaProducerTemplate<String, ClientEvent>(SenderOptions.create(props));
    }
}
