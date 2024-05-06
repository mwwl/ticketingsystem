package com.distalgo.client.service;

import com.distalgo.saga.events.CallbackEvent;
import com.distalgo.saga.events.InventoryEvent;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ClientConsumerService {
    // note: consuming callback event
    private final ReactiveKafkaConsumerTemplate<String, CallbackEvent> callbackEventConsumerKafkaTemplate;

    @Autowired
    private ClientService clientService;

    public ClientConsumerService(ReactiveKafkaConsumerTemplate<String, CallbackEvent> callbackEventConsumerKafkaTemplate) {
        this.callbackEventConsumerKafkaTemplate = callbackEventConsumerKafkaTemplate;
    }

    /**
     * Runs automatically after application context has been initialized
     */
    @PostConstruct
    public void startConsumingOrderEvents() {
        consumeCallbackEvent().subscribe();
    }

    /**
     * Consumed callback event, to update the GUI accordingly
     * @return
     */
    private Flux<CallbackEvent> consumeCallbackEvent() {
        return callbackEventConsumerKafkaTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .doOnNext(callbackEvent -> {
                    System.out.println("within ClientConsumerService, received: " + callbackEvent);
                    clientService.updateClient(callbackEvent);
                });
    }
}
