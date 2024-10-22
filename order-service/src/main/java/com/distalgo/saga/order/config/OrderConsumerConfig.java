package com.distalgo.saga.order.config;

import com.distalgo.saga.events.Event;
import com.distalgo.saga.events.ClientEvent;
import com.distalgo.saga.events.InventoryEvent;
import com.distalgo.saga.events.PaymentEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.router.RecipientListRouter;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

@Configuration
public class OrderConsumerConfig {
    @Bean
    public ReceiverOptions<String, Event> kafkaReceiverInvOrPayment(@Value(value = "order-updates") String topic, KafkaProperties kafkaProperties) {
        ReceiverOptions<String, Event> basicOptions = ReceiverOptions.<String, Event>create(kafkaProperties.buildConsumerProperties(null));
        return basicOptions.subscription(Collections.singletonList(topic));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, Event> invOrPaymentEventConsumerKafkaTemplate(ReceiverOptions<String, Event> receiverOptions) {
        return new ReactiveKafkaConsumerTemplate<String, Event>(receiverOptions);
    }


    @Bean
    public ReceiverOptions<String, ClientEvent> kafkaReceiverClient(@Value(value = "client-event") String topic, KafkaProperties kafkaProperties) {
        ReceiverOptions<String, ClientEvent> basicOptions = ReceiverOptions.<String, ClientEvent>create(kafkaProperties.buildConsumerProperties(null));
        return basicOptions.subscription(Collections.singletonList(topic));
    }

    @Bean
    public ReactiveKafkaConsumerTemplate<String, ClientEvent> clientEventConsumerKafkaTemplate(ReceiverOptions<String, ClientEvent> receiverOptions) {
        return new ReactiveKafkaConsumerTemplate<String, ClientEvent>(receiverOptions);
    }
}

