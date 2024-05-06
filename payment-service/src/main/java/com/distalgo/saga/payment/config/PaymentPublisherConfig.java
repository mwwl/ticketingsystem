package com.distalgo.saga.payment.config;

import com.distalgo.saga.events.PaymentEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

@Configuration
public class PaymentPublisherConfig {
    @Bean
    public ReactiveKafkaProducerTemplate<String, PaymentEvent> paymentEventProducerKafkaTemplate(KafkaProperties properties) {
        Map<String, Object> props = properties.buildProducerProperties(null);

        // do more research on this
        // props.put("max.poll.interval.ms", "30000"); --> change the time
        // props.put("max.poll.records", "1"); --> change this number
        return new ReactiveKafkaProducerTemplate<String, PaymentEvent>(SenderOptions.create(props));
    }
}
