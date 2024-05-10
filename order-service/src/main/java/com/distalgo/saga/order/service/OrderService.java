package com.distalgo.saga.order.service;

import com.distalgo.saga.dto.OrderCallbackDTO;
import com.distalgo.saga.dto.OrderRequestDTO;
import com.distalgo.saga.events.*;
import com.distalgo.saga.order.entity.OrderEntity;
import com.distalgo.saga.order.repo.OrderRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private OrderPublisherService orderPublisherService;

    private HashMap<Integer, Integer> eventPricelist;


    /**
     * Creates the actual order to be saved in the repository,
     * and publishes the order event to the kafka topic (order-event)
     *
     *
     * Still need to have an argument to give the sessionID to be saved
     */
    @Transactional
    public void createOrder(OrderRequestDTO orderRequestDTO, String sessionID) {
        OrderEntity orderToSave = convertDTOToEntity(orderRequestDTO);
        orderToSave.setSessionID(sessionID);

        OrderEntity orderSaved = orderRepo.save(orderToSave);
//        logger.info("Saved order in repo: {}", orderSaved);
        System.out.println("Saved order in repo: " + orderSaved);
        orderRequestDTO.setOrderID(orderSaved.getOrderID());
        orderPublisherService.publishOrderEvent(orderRequestDTO, OrderStatus.ORDER_CREATED);
    }

    /**
     * Finds the order in the repo through the order ID, and update the order status and the inventory status to failed
     * it is noted that an inventory event coming in would always be INVENTORY_CHECK_FAILED
     *
     * Will send this back ot the client (inventory check failed, order failed)
     *
     * after consuming the inventory event (failed inventory check), have to publish it to the callback-event topic
     *
     * @param inventoryEvent
     */
    @Transactional
    public void updateOrder(InventoryEvent inventoryEvent) {
        // finds order in the repo
        orderRepo.findById(inventoryEvent.getInventoryRequestDTO().getOrderID())
                .ifPresent(orderRecord -> {
                    setStatuses(orderRecord, OrderStatus.ORDER_FAILED, inventoryEvent.getInventoryStatus(), null);
                    orderRepo.save(orderRecord);
                    OrderCallbackDTO callbackDTO = constructCallbackDTO(orderRecord);
                    orderPublisherService.publishCallbackEvent(callbackDTO, orderRecord.getSessionID());
                });
    }

    /**
     * What is updated depends on the payment event status -- it could be a failure, or it could be a success
     * <p>
     * If failure, the order then has to publish the order_cancelled to the inventory microservice as order_cancelled
     * If success, the order is completed -- order_success, and user will know
     * <p>
     * inventory check success, but payment event may or may not have failed
     *
     * Regardless of success or failure, it will always publish to the callback event, but only when there's a
     * failure, will it publish it to the order-event for the inventory service to consume
     *
     * @param paymentEvent
     */
    @Transactional
    public void updateOrder(PaymentEvent paymentEvent) {
        PaymentStatus paymentStatus = paymentEvent.getPaymentStatus();

        orderRepo.findById(paymentEvent.getPaymentRequestDTO().getOrderID())
                .ifPresent(orderRecord -> {
//                    System.out.println("order (after receiving payment) retrieved from repo: " + orderRecord);
                    if (paymentStatus.equals(PaymentStatus.PAYMENT_SUCCESS)) {
                        // payment has succeeded, update order record accordingly
                        setStatuses(orderRecord, OrderStatus.ORDER_SUCCESS, InventoryStatus.INVENTORY_CHECK_SUCCESS, paymentStatus);
//                        logger.info("Payment successful");
                        System.out.println("Payment successful");
                    } else {
                        // payment has failed, so need to update the order record accordingly, and send this to the order-event for inventory service to do compensating action
                        setStatuses(orderRecord, OrderStatus.ORDER_FAILED, InventoryStatus.INVENTORY_CHECK_SUCCESS, paymentStatus);
                        OrderRequestDTO orderRequestDTO = constructOrderRequestDTO(orderRecord);
//                        logger.info("Payment failed");
                        System.out.println("Payment failed");

                        orderPublisherService.publishOrderEvent(orderRequestDTO, OrderStatus.ORDER_FAILED);
                    }
                    orderRepo.save(orderRecord);
                    OrderCallbackDTO callbackDTO = constructCallbackDTO(orderRecord);
                    orderPublisherService.publishCallbackEvent(callbackDTO, orderRecord.getSessionID());
                });
    }


    public Integer getTicketPrice(Integer eventID) {
        Integer price = eventPricelist.get(eventID);

        if (price == null) {
            price = 0;
        }
//        logger.info("Ticket price (1): {}", price);
        System.out.println("Price of 1 ticket: " + price);
        return price;
    }

    public List<OrderEntity> getAllOrders(){
        return orderRepo.findAll();
    }

    private void setStatuses(OrderEntity orderRecord, OrderStatus orderStatus, InventoryStatus inventoryStatus,
                             PaymentStatus paymentStatus) {
        orderRecord.setOrderStatus(orderStatus);
        orderRecord.setInventoryStatus(inventoryStatus);  // know this because it will only receive a payment event from this topic if the inventory was successful
        orderRecord.setPaymentStatus(paymentStatus);
    }

    private OrderRequestDTO constructOrderRequestDTO(OrderEntity orderRecord) {
        return new OrderRequestDTO(orderRecord.getUserID(), orderRecord.getEventID(), orderRecord.getSeats(),
                orderRecord.getPrice(), orderRecord.getOrderID());
    }

    private OrderCallbackDTO constructCallbackDTO(OrderEntity orderRecord) {
        return new OrderCallbackDTO(orderRecord.getOrderID(), orderRecord.getOrderStatus(),
                orderRecord.getInventoryStatus(), orderRecord.getPaymentStatus());
    }

    private OrderEntity convertDTOToEntity(OrderRequestDTO orderRequestDTO) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserID(orderRequestDTO.getUserID());
        orderEntity.setSessionID(orderEntity.getSessionID());
        orderEntity.setEventID(orderRequestDTO.getTicketedEventID());
        orderEntity.setPrice(orderRequestDTO.getAmount());
        orderEntity.setSeats(orderRequestDTO.getSeats());
        orderEntity.setOrderStatus(OrderStatus.ORDER_CREATED);
        return orderEntity;
    }

    // added event ticket list
    @PostConstruct
    private void eventTicketList() {
        eventPricelist = new HashMap<>();
        eventPricelist.put(10100, 10);
        eventPricelist.put(10200, 30);
        eventPricelist.put(10300, 5);
        eventPricelist.put(10400, 20);
        eventPricelist.put(10500, 1);
    }
}
