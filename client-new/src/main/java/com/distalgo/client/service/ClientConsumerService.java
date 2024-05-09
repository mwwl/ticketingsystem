package com.distalgo.client.service;

import com.distalgo.saga.events.CallbackEvent;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;

@Service
@EnableScheduling
public class ClientConsumerService {
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
    public void startConsumingCallbackEvents() {
        consumeCallbackEvent().subscribe();
    }

    /**
     * Consumed callback event, to update the GUI accordingly
     *
     * Have to filter out the messages that were meant for this person -- using sessionID (have to think how to get the sessionID though...)
     */
    private Flux<CallbackEvent> consumeCallbackEvent() {
        return callbackEventConsumerKafkaTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .filter(callbackEvent -> callbackEvent.getSessionID().equals(clientService.getSessionID()))
                .doOnNext(callbackEvent -> {
                    System.out.println("within ClientConsumerService, received: " + callbackEvent);
                    clientService.updateClient(callbackEvent);
                });
    }

    /**
     * Checks every 30s if a set time has passed since the last published message, of the client, and
     *
     * Testing every 10 seconds for now
     */
    @Scheduled(fixedDelay = 10000)
    private void checkUnconsumed() {
        System.out.println("checking unconsumed");
        Instant lastTimestamp = clientService.getLastPublishedTime();
        Instant currentTimeStamp = Instant.now();
        String clientUpdate = "One of the services is down, your tickets are tentatively booked, " +
                "pending seat availability and/or payment.\n";

        if (lastTimestamp != null) {
            Duration timePassed = Duration.between(lastTimestamp, currentTimeStamp);
            if (timePassed.toMillis() > 10000) {
                System.out.println("current time stamp: " + currentTimeStamp);
                clientService.updateClient(clientUpdate);
                clientService.saveCurrentTime(null);
            }
        }
    }
}