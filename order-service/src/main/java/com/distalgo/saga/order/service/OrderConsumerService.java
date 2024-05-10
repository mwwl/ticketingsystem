package com.distalgo.saga.order.service;

import com.distalgo.saga.dto.ClientRequestDTO;
import com.distalgo.saga.dto.OrderRequestDTO;
import com.distalgo.saga.events.*;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class OrderConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(OrderConsumerService.class);
    private final ReactiveKafkaConsumerTemplate<String, ClientEvent> clientEventConsumerKafkaTemplate;
    private final ReactiveKafkaConsumerTemplate<String, Event> eventConsumerKafkaTemplate;

    @Autowired
    OrderService orderService;

    public OrderConsumerService(ReactiveKafkaConsumerTemplate<String, ClientEvent> clientEventConsumerKafkaTemplate,
                                ReactiveKafkaConsumerTemplate<String, Event> eventConsumerKafkaTemplate) {
        this.clientEventConsumerKafkaTemplate = clientEventConsumerKafkaTemplate;
        this.eventConsumerKafkaTemplate = eventConsumerKafkaTemplate;
    }

    /**
     * Runs automatically after application context has been initialized
     */
    @PostConstruct
    public void startConsumingOrderEvents() {
        consumeClientEvent().subscribe();
        consumeInventoryOrPaymentEvent().subscribe();
        System.out.println("Subscribed to each of the event consumers - client and inventory/payment.");
    }

    /**
     * Consumed client event, to start order
     *
     * @return
     */
    private Flux<ClientEvent> consumeClientEvent() {
        return clientEventConsumerKafkaTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .doOnNext(clientEvent -> {
//                    logger.info("Received client event: {}", clientEvent);
                    System.out.println("Received client event: " + clientEvent);
                    startOrder(clientEvent);
                });
    }

    private Flux<Void> consumeInventoryOrPaymentEvent() {
        return eventConsumerKafkaTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .flatMap(this::processEvent);
    }

    private Mono<Void> processEvent(Event event) {
        if (event instanceof InventoryEvent) {
//            logger.info("Handling inventory event");
            System.out.println("Handling inventory event");
            return handleInventoryEvent((InventoryEvent) event);
        } else if (event instanceof PaymentEvent) {
//            logger.info("Handling payment event");
            System.out.println("Handling payment event");
            return handlePaymentEvent((PaymentEvent) event);
        } else {
            return Mono.empty();
        }
    }

    private Mono<Void> handleInventoryEvent(InventoryEvent inventoryEvent) {
        return Mono.fromRunnable(() -> {
            orderService.updateOrder(inventoryEvent);
        });
    }

    private Mono<Void> handlePaymentEvent(PaymentEvent paymentEvent) {
        return Mono.fromRunnable(() -> {
            orderService.updateOrder(paymentEvent);
        });
    }

    /**
     * Order service receives from the client, and starts/create an order
     *
     * the order service will (use eventPublisherService) to publish the order event to the order-event topic to
     * be consumed by the Inventory Service
     * @param clientEvent
     */
    private void startOrder(ClientEvent clientEvent) {
//        logger.info("Started order for client event: {}" , clientEvent);
        System.out.println("Started order for client event: " + clientEvent);
        ClientRequestDTO clientRequestDTO = clientEvent.getClientRequestDTO();
        OrderRequestDTO orderRequestDTO = constructOrderRequestDTO(clientRequestDTO);
        orderService.createOrder(orderRequestDTO, clientEvent.getClientRequestDTO().getSessionID());
    }

    /**
     * Currently, order ID is null, because it hasn't been saved into the database yet
     *
     * @param clientRequestDTO
     * @return
     */
    private OrderRequestDTO constructOrderRequestDTO(ClientRequestDTO clientRequestDTO) {
        Integer ticketPrice = orderService.getTicketPrice(clientRequestDTO.getEventID());
        Integer amount = clientRequestDTO.getSeats() * ticketPrice;
        return new OrderRequestDTO(clientRequestDTO.getUserID(), clientRequestDTO.getEventID(),
                clientRequestDTO.getSeats(), amount, null);
    }
}