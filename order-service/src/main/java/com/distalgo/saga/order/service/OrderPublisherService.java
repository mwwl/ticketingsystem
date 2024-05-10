package com.distalgo.saga.order.service;

import com.distalgo.saga.dto.OrderCallbackDTO;
import com.distalgo.saga.dto.OrderRequestDTO;
import com.distalgo.saga.events.CallbackEvent;
import com.distalgo.saga.events.OrderEvent;
import com.distalgo.saga.events.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderPublisherService {
    private final ReactiveKafkaProducerTemplate<String, OrderEvent> orderEventProducerKafkaTemplate;
    private final ReactiveKafkaProducerTemplate<String, CallbackEvent> callbackEventProducerKafkaTemplate;

    @Value(value = "order-event")
    private String orderTopic;

    @Value(value = "callback-event")
    private String callbackTopic;

    public OrderPublisherService(ReactiveKafkaProducerTemplate<String, OrderEvent> orderEventProducerKafkaTemplate, ReactiveKafkaProducerTemplate<String, CallbackEvent> callbackEventProducerKafkaTemplate) {
        this.orderEventProducerKafkaTemplate = orderEventProducerKafkaTemplate;
        this.callbackEventProducerKafkaTemplate = callbackEventProducerKafkaTemplate;
    }

    public void publishOrderEvent(OrderRequestDTO orderRequestDTO, OrderStatus orderStatus) {
        OrderEvent orderEvent = new OrderEvent(orderRequestDTO, orderStatus);
        orderEventProducerKafkaTemplate.send(orderTopic, orderEvent)
                .doOnSuccess(sendResult -> System.out.println("Sending to order-event: " + orderEvent))
                .subscribe();
    }

    public void publishCallbackEvent(OrderCallbackDTO orderCallbackDTO, String sessionID) {
        CallbackEvent callbackEvent = new CallbackEvent(orderCallbackDTO);
        callbackEvent.setSessionID(sessionID);
        callbackEventProducerKafkaTemplate.send(callbackTopic, callbackEvent)
                .doOnSuccess(sendResult -> System.out.println("Sending to callback-event: " + callbackEvent))
                .subscribe();
    }
}
