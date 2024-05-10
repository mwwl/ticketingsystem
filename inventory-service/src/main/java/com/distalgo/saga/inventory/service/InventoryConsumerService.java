package com.distalgo.saga.inventory.service;

import com.distalgo.saga.events.InventoryEvent;
import com.distalgo.saga.events.OrderEvent;
import com.distalgo.saga.events.OrderStatus;
import com.distalgo.saga.inventory.repo.EventInventoryRepo;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Consumes order-event and validates the inventory
 */
@Service
public class InventoryConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryConsumerService.class);
    private final ReactiveKafkaConsumerTemplate<String, OrderEvent> orderEventConsumerKafkaTemplate;

    @Autowired
    private EventInventoryRepo eventInventoryRepo;

    @Autowired
    private InventoryValidationService inventoryValidationService;

    @Autowired
    private InventoryEventPublisher inventoryEventPublisher;

    public InventoryConsumerService(ReactiveKafkaConsumerTemplate<String, OrderEvent> orderEventConsumerKafkaTemplate) {
        this.orderEventConsumerKafkaTemplate = orderEventConsumerKafkaTemplate;
    }

    // initiated at the time of application start up
//    @PostConstruct
//    public void initEventInventoryInDB() {
//        eventInventoryRepo.saveAll(Stream.of(
//                new EventInventory(10100, 5000),
//                new EventInventory(10200, 100),
//                new EventInventory(10300, 500),
//                new EventInventory(10400, 300),
//                new EventInventory(10500, 10)).collect(Collectors.toList()));
//    }

    /**
     * Runs automatically after application context has been initialized
     */
    @PostConstruct
    public void startConsumingOrderEvents() {
        consumeOrderEvent().subscribe();
    }


    /**
     * Returns a flux of the order event consumed from the order-event topic, does stuff within it -- where the logic is
     */
    private Flux<OrderEvent> consumeOrderEvent() {
        return orderEventConsumerKafkaTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .doOnNext(orderEvent -> {
//                    logger.info("received: {}", orderEvent);
                    System.out.println("Received Order Event: " + orderEvent);
                    validateInventory(orderEvent);
                });
    }

    /**
     * Checks whether the order status in the orderEvent object is CREATED or anything else
     *
     * if created, then it will create an inventory event (including creating the inventory DTO) bc its a new order,
     * to be put onto the topic payment-event (INVENTORY_SUCCESS or INVENTORY_FAILED)
     * will also save a inventory transaction into the repo
     *
     * else, it will delete the inventory transaction repo (if present), and add the inventory back into the database
     */
    private void validateInventory(OrderEvent orderEvent) {
        if (orderEvent.getOrderStatus().equals(OrderStatus.ORDER_CREATED)) {
            InventoryEvent inventoryEvent = inventoryValidationService.newOrderEvent(orderEvent);
            inventoryEventPublisher.publishInventoryEvent(inventoryEvent);
        } else { // just removed some comments here, no big change
            System.out.println("Performing compensation for this order (#" + orderEvent.getOrderRequestDTO().getOrderID() + ")");
            inventoryValidationService.cancelOrderEvent(orderEvent);
        }
    }
}