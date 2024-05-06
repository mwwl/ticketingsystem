package com.distalgo.saga.payment.service;


import com.distalgo.saga.events.InventoryEvent;
import com.distalgo.saga.events.InventoryStatus;
import com.distalgo.saga.events.PaymentEvent;
import com.distalgo.saga.payment.repo.UserBalanceRepo;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Consumes inventory event from inventory-event topic
 *
 * When consume event, need to check the repo (user balance) to see whether the user have enough balance to
 * actually purchase the ticket
 *
 * Optimistic view where more often than not, the users will have sufficient balance in their accounts
 */
@Service
public class PaymentConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentConsumerService.class);
    private final ReactiveKafkaConsumerTemplate<String, InventoryEvent> inventoryEventConsumerKafkaTemplate;

    @Autowired
    private UserBalanceRepo userBalanceRepo;

    @Autowired
    private PaymentValidationService paymentValidationService;

    @Autowired
    private PaymentEventPublisher paymentEventPublisher;

    public PaymentConsumerService(ReactiveKafkaConsumerTemplate<String, InventoryEvent> inventoryEventConsumerKafkaTemplate) {
        this.inventoryEventConsumerKafkaTemplate = inventoryEventConsumerKafkaTemplate;
    }

    /**
     * Runs automatically after application context has been initialized
     */
    @PostConstruct
    public void startConsumingInventoryEvents() {
        consumeInventoryEvent().subscribe();
    }


    /**
     * Returns a flux of the inventory event consumed from the inventory-event topic,
     * and does stuff within it -- where the logic is
     */
    private Flux<InventoryEvent> consumeInventoryEvent() {
        return inventoryEventConsumerKafkaTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .doOnNext(inventoryEvent -> {
                    logger.info("received: {}", inventoryEvent);
                    validateBalance(inventoryEvent);
                });
    }

    /**
     * Validates the user balance
     *
     * Checks whether the inventory has been successful or not (or if its cancelled), and if it has been, then
     * deduct from balance, and create a new transaction in the user transaction repo, and also create a payment
     * event (including the payment DTO) to be put onto the event-updates topic with PAYMENT_SUCCESS or PAYMENT_FAILED
     *
     * If the inventory check had been unsuccessful or cancelled, then it will delete the user transaction in the
     * repo if present, and add the balance back into the db
     *
     */
    private void validateBalance(InventoryEvent inventoryEvent) {
        if (inventoryEvent.getInventoryStatus().equals(InventoryStatus.INVENTORY_CHECK_SUCCESS)) {
            // have a new inventory event, create a payment event (in the inventoryValidationService)
            PaymentEvent paymentEvent = paymentValidationService.newInventoryEvent(inventoryEvent);

            // then publish the result into the event-updates topic
            paymentEventPublisher.publishPaymentEvent(paymentEvent);
        } else {
            // cancel the inventory event - compensation
            paymentValidationService.cancelInventoryEvent(inventoryEvent);
        }
    }


//    // initiiated at the time of application start up
//    @PostConstruct
//    public void initUserBalanceInDB() {
//        userBalanceRepo.saveAll(Stream.of(
//                new UserBalance(100, 5000),
//                new UserBalance(101, 2000),
//                new UserBalance(102, 150),
//                new UserBalance(103, 400),
//                new UserBalance(104, 150),
//                new UserBalance(105, 4000)).collect(Collectors.toList()));
//    }
}
