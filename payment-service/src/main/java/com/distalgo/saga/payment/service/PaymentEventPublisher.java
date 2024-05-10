package com.distalgo.saga.payment.service;

import com.distalgo.saga.events.PaymentEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventPublisher {
    private final ReactiveKafkaProducerTemplate<String, PaymentEvent> paymentEventProducerKafkaTemplate;

    @Value(value = "order-updates")
    private String topic;

    public PaymentEventPublisher(ReactiveKafkaProducerTemplate<String, PaymentEvent> paymentEventProducerKafkaTemplate) {
        this.paymentEventProducerKafkaTemplate = paymentEventProducerKafkaTemplate;
    }

    public void publishPaymentEvent(PaymentEvent paymentEvent) {
        paymentEventProducerKafkaTemplate.send(topic, paymentEvent)
                .doOnSuccess(sendResult -> System.out.println("Sent to topic (order-updates) from payment service: " + paymentEvent))
                .subscribe();
    }
}
