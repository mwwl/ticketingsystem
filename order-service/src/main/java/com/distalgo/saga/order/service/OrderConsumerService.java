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

@Service
public class OrderConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(OrderConsumerService.class);
    private final ReactiveKafkaConsumerTemplate<String, InventoryEvent> inventoryEventConsumerKafkaTemplate;
    private final ReactiveKafkaConsumerTemplate<String, PaymentEvent> paymentEventConsumerKafkaTemplate;
    private final ReactiveKafkaConsumerTemplate<String, ClientEvent> clientEventConsumerKafkaTemplate;
    private final ReactiveKafkaConsumerTemplate<String, Event> eventConsumerKafkaTemplate;

    @Autowired
    OrderService orderService;


    public OrderConsumerService(ReactiveKafkaConsumerTemplate<String, InventoryEvent> inventoryEventConsumerKafkaTemplate,
                                ReactiveKafkaConsumerTemplate<String, PaymentEvent> paymentEventConsumerKafkaTemplate,
                                ReactiveKafkaConsumerTemplate<String, ClientEvent> clientEventConsumerKafkaTemplate, ReactiveKafkaConsumerTemplate<String, Event> eventConsumerKafkaTemplate) {
        this.inventoryEventConsumerKafkaTemplate = inventoryEventConsumerKafkaTemplate;
        this.paymentEventConsumerKafkaTemplate = paymentEventConsumerKafkaTemplate;
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
        System.out.println("subscribed to each of the event consumers - client, inventory, and payment");
    }

    /**
     * Consumed client event, to start order
     *
     * @return
     */
    private Flux<ClientEvent> consumeClientEvent() {
        System.out.println("CONSUME CLIENT EVENT");
        return clientEventConsumerKafkaTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .doOnNext(clientEvent -> {
                    logger.info("within order consumer, consume client event, consuming type: {}", String.valueOf(clientEvent.getClass()));
                    logger.info("received: {}", clientEvent);
                    startOrder(clientEvent);
                });
    }

    private Flux<Void> consumeInventoryOrPaymentEvent() {
        System.out.println("CONSUMING EITHER INVENTORY OR PAYMENT EVENT");
        return eventConsumerKafkaTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .flatMap(this::processEvent);
    }

    private Mono<Void> processEvent(Event event) {
        if (event instanceof InventoryEvent) {
            System.out.println("Turns out, its an inventory event");
            return handleInventoryEvent((InventoryEvent) event);
        } else if (event instanceof PaymentEvent) {
            System.out.println("Turns out, its a payment event");
            return handlePaymentEvent((PaymentEvent) event);
        } else {
            return Mono.empty();
        }
    }

    private Mono<Void> handleInventoryEvent(InventoryEvent inventoryEvent) {
        return Mono.fromRunnable(() -> {
            System.out.println("received in handleInventoryEvent: " + inventoryEvent);
            orderService.updateOrder(inventoryEvent);
        });
    }

    private Mono<Void> handlePaymentEvent(PaymentEvent paymentEvent) {
        return Mono.fromRunnable(() -> {
            System.out.println("received in handlePaymentEvent: " + paymentEvent);
            orderService.updateOrder(paymentEvent);
        });
    }



    /**
     * Consumed inventory event, to update order
     *
     * @return
     */
    private Flux<InventoryEvent> consumeInventoryEvent() {
        System.out.println("CONSUME INVENTORY EVENT");
        return inventoryEventConsumerKafkaTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .doOnNext(inventoryEvent -> {
                    logger.info("within order consumer, consume inventory event, consuming type: {}", String.valueOf(inventoryEvent.getClass()));
                    logger.info("received: {}", inventoryEvent);
                    orderService.updateOrder(inventoryEvent);
                });
    }

    /**
     * Consumed payment event, to update order
     *
     * @return
     */
    private Flux<PaymentEvent> consumePaymentEvent() {
        System.out.println("CONSUME PAYMENT EVENT");
        return paymentEventConsumerKafkaTemplate
                .receiveAutoAck()
                .map(ConsumerRecord::value)
                .doOnNext(paymentEvent -> {
                    logger.info("within order consumer, consume payment event, consuming type: {}", String.valueOf(paymentEvent.getClass()));
                    logger.info("received: {}", paymentEvent);
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
        System.out.println("started the order for this client event: " + clientEvent);
        ClientRequestDTO clientRequestDTO = clientEvent.getClientRequestDTO();
        OrderRequestDTO orderRequestDTO = constructOrderRequestDTO(clientRequestDTO);
        System.out.println("created orderRequestDTO in startOrder: " + orderRequestDTO);
        orderService.createOrder(orderRequestDTO, clientEvent.getClientRequestDTO().getSessionID());
    }

//    /**
//     * Finds the order in the repo through the order ID, and update the order status and the inventory status to failed
//     * it is noted that an inventory event coming in would always be INVENTORY_CHECK_FAILED
//     *
//     * Will send this back ot the client (inventory check failed, order failed)
//     *
//     * after consuming the inventory event (failed inventory check), have to publish it to the callback-event topic
//     *
//     * @param inventoryEvent
//     */
//    private void updateOrder(InventoryEvent inventoryEvent) {
//        System.out.println("UPDATE ORDER, EXPECTING INVENTORY EVENT");
//        // finds order in the repo
//        orderRepo.findById(inventoryEvent.getInventoryRequestDTO().getOrderID())
//                .ifPresent(orderRecord -> {
//                    logger.info("order retrieved from repo: " + orderRecord);
//                    orderRecord.setOrderStatus(OrderStatus.ORDER_FAILED);
//                    orderRecord.setInventoryStatus(inventoryEvent.getInventoryStatus());
//                    orderRepo.save(orderRecord);
//                    logger.info("order after: " + orderRecord);
//                    // TODO: SEND BACK TO THE CLIENT HERE TOO -- INVENTORY CHECK FAILED
//                });
//    }

//    /**
//     * What is updated depends on the payment event status -- it could be a failure, or it could be a success
//     * <p>
//     * If failure, the order then has to publish the order_cancelled to the inventory microservice as order_cancelled
//     * If success, the order is completed -- order_success, and user will know
//     * <p>
//     * inventory check success, but payment event may or may not have failed
//     *
//     * @param paymentEvent
//     */
//    private void updateOrder(PaymentEvent paymentEvent) {
//        System.out.println("UPDATE ORDER, EXPECTING PAYMENT EVENT");
//        PaymentStatus paymentStatus = paymentEvent.getPaymentStatus();
//        System.out.println("PAYMENT STATUS: " + paymentStatus);
//        orderRepo.findById(paymentEvent.getPaymentRequestDTO().getOrderID())
//                .ifPresent(orderRecord -> {
//                    logger.info("order (after receiving payment) retrieved from repo: " + orderRecord);
//                    if (paymentStatus.equals(PaymentStatus.PAYMENT_SUCCESS)) {
//                        // payment has succeeded, update order record accordingly
//                        orderRecord.setOrderStatus(OrderStatus.ORDER_SUCCESS);
//                        System.out.println("set payment status when payment success: " + paymentStatus);
//                        orderRecord.setPaymentStatus(paymentStatus);
//                        orderRecord.setInventoryStatus(InventoryStatus.INVENTORY_CHECK_SUCCESS);  // know this because it will only receive a payment event from this topic if the inventory was successful
//                        orderRepo.save(orderRecord);
//                        // TODO: order success, let user know - have the IP and port, now need to check when sending order status, how to pass that in/how to get the arguments out from the orderrepo (order entity)
//                        // if it works, do it for the payment fail part below
//                        OrderCallbackDTO orderCallbackDTO = constructOrderCallbackDTO(orderRecord);
////                        orderController.sendOrderStatus(orderRecord.getSessionID(), orderCallbackDTO,
////                                orderRecord.getClientIP(), orderRecord.getClientPort());
//
//                    } else {
//                        // payment has failed, so need to update the order record accordingly, and send this to the order-event for inventory service to do compensating action
//                        orderRecord.setOrderStatus(OrderStatus.ORDER_FAILED);
//                        System.out.println("set payment status when payment did not succeed: " + paymentStatus);
//                        orderRecord.setPaymentStatus(paymentStatus);
//                        orderRecord.setInventoryStatus(InventoryStatus.INVENTORY_CHECK_SUCCESS);
//                        orderRepo.save(orderRecord);
//                        OrderRequestDTO orderRequestDTO = constructOrderRequestDTO(orderRecord);
//                        orderPublisherService.publishOrderEvent(orderRequestDTO, OrderStatus.ORDER_FAILED);
//                        logger.info("sent order event to order-event topic for the inventory service to do compensation");
//                        // TODO: payment failed, let user know (use controller to send and send the orderRecord)
//                    }
//                    logger.info("order after (after receiving payment): " + orderRecord);
//                });
//    }
//
//    private OrderRequestDTO constructOrderRequestDTO(OrderEntity orderRecord) {
//        return new OrderRequestDTO(orderRecord.getUserID(),
//                orderRecord.getEventID(), orderRecord.getSeats(), orderRecord.getPrice(), orderRecord.getOrderID());
//    }

    /**
     * Currently, order ID is null, because it hasn't been saved into the database yet
     *
     * @param clientRequestDTO
     * @return
     */
    private OrderRequestDTO constructOrderRequestDTO(ClientRequestDTO clientRequestDTO) {
        Integer ticketPrice = getTicketPrice(clientRequestDTO.getEventID());
        Integer amount = clientRequestDTO.getSeats() * ticketPrice;
        return new OrderRequestDTO(clientRequestDTO.getUserID(), clientRequestDTO.getEventID(),
                clientRequestDTO.getSeats(), amount, null);
    }

//    private OrderCallbackDTO constructOrderCallbackDTO(OrderEntity orderRecord) {
//        return new OrderCallbackDTO(orderRecord.getOrderID(), orderRecord.getOrderStatus(),
//                orderRecord.getInventoryStatus(), orderRecord.getPaymentStatus());
//    }

    /**
     * For now its $10 for all events, but if there's time, can use a table or something
     * @param eventID
     * @return
     */
    private Integer getTicketPrice(Integer eventID) {
        return 10;
    }
}