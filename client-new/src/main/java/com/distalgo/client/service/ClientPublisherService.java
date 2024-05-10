package com.distalgo.client.service;

import com.distalgo.saga.dto.ClientRequestDTO;
import com.distalgo.saga.events.ClientEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Publishes a client event to the client-event topic
 */
@Service
public class ClientPublisherService {
    private final static int USER_ID_IDX = 0;
    private final static int EVENT_ID_IDX = 1;
    private final static int NUM_SEATS_IDX = 2;

    private final ReactiveKafkaProducerTemplate<String, ClientEvent> clientEventProducerKafkaTemplate;

    public ClientPublisherService(ReactiveKafkaProducerTemplate<String, ClientEvent> clientEventProducerKafkaTemplate) {
        this.clientEventProducerKafkaTemplate = clientEventProducerKafkaTemplate;
    }

    @Value(value = "client-event")
    private String topic;

    @Autowired
    ClientService clientService;


    public void publishClientEvent(String sessionID, ArrayList<Integer> input) {
        ClientRequestDTO clientRequestDTO = createClientRequestDTO(sessionID, input);
        ClientEvent clientEvent = new ClientEvent(clientRequestDTO);

        clientEventProducerKafkaTemplate.send(topic, clientEvent)
                .doOnSuccess(sendResult -> {
                    System.out.println("Sent from client: " + clientEvent);
                    clientService.saveCurrentTime(Instant.now());
                })
                .subscribe();
    }

    private ClientRequestDTO createClientRequestDTO(String sessionID, ArrayList<Integer> input) {
        return new ClientRequestDTO(sessionID, input.get(USER_ID_IDX), input.get(EVENT_ID_IDX), input.get(NUM_SEATS_IDX));
    }
}
