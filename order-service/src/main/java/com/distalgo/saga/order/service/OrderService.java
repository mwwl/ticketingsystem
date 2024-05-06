package com.distalgo.saga.order.service;

import com.distalgo.saga.dto.OrderCallbackDTO;
import com.distalgo.saga.dto.OrderRequestDTO;
import com.distalgo.saga.events.*;
import com.distalgo.saga.order.entity.OrderEntity;
import com.distalgo.saga.order.repo.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private OrderPublisherService orderPublisherService;


    /**
     * Creates the actual order to be saved in the repository,
     * and publishes the order event to the kafka topic (order-event)
     *
     *
     * Still need to have an argument to give the sessionID to be saved
     */
    @Transactional
    public OrderEntity createOrder(OrderRequestDTO orderRequestDTO) {
        System.out.println("created order, now saving");
        OrderEntity orderToSave = convertDTOToEntity(orderRequestDTO);

        OrderEntity orderSaved = orderRepo.save(orderToSave);
        System.out.println("order saved: " + orderSaved);
        orderRequestDTO.setOrderID(orderSaved.getOrderID());
        System.out.println("going to publish order event");

        orderPublisherService.publishOrderEvent(orderRequestDTO, OrderStatus.ORDER_CREATED);

        // is this order to save returned last after everything is done?? run a failed inventory check, and see what is returned first
        return orderToSave;
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
        System.out.println("UPDATE ORDER, EXPECTING INVENTORY EVENT");
        System.out.println("UPDATE ORDER, EXPECTING INVENTORY EVENT");
        // finds order in the repo
        orderRepo.findById(inventoryEvent.getInventoryRequestDTO().getOrderID())
                .ifPresent(orderRecord -> {
                    System.out.println("order retrieved from repo: " + orderRecord);
                    setStatuses(orderRecord, OrderStatus.ORDER_FAILED, inventoryEvent.getInventoryStatus(), null);
                    orderRepo.save(orderRecord);
                    System.out.println("order after saving to repo: " + orderRecord);
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
     * @param paymentEvent
     */
    @Transactional
    public void updateOrder(PaymentEvent paymentEvent) {
        System.out.println("UPDATE ORDER, EXPECTING PAYMENT EVENT");
        PaymentStatus paymentStatus = paymentEvent.getPaymentStatus();
        System.out.println("PAYMENT STATUS: " + paymentStatus);

        orderRepo.findById(paymentEvent.getPaymentRequestDTO().getOrderID())
                .ifPresent(orderRecord -> {
//                    System.out.println("order (after receiving payment) retrieved from repo: " + orderRecord);
                    if (paymentStatus.equals(PaymentStatus.PAYMENT_SUCCESS)) {
                        // payment has succeeded, update order record accordingly
                        System.out.println("before setting status after successful payment: " + orderRecord);
                        setStatuses(orderRecord, OrderStatus.ORDER_SUCCESS, InventoryStatus.INVENTORY_CHECK_SUCCESS, paymentStatus);
                        System.out.println("after setting status after successful payment: " + orderRecord);
                    } else {
                        // payment has failed, so need to update the order record accordingly, and send this to the order-event for inventory service to do compensating action
                        setStatuses(orderRecord, OrderStatus.ORDER_FAILED, InventoryStatus.INVENTORY_CHECK_SUCCESS, paymentStatus);
                    }
                    orderRepo.save(orderRecord);
                    System.out.println("newly updated order, going to publish to callback-event topic: " + orderRecord);
                    OrderCallbackDTO callbackDTO = constructCallbackDTO(orderRecord);
                    orderPublisherService.publishCallbackEvent(callbackDTO, orderRecord.getSessionID());
                });
    }

    private void setStatuses(OrderEntity orderRecord, OrderStatus orderStatus, InventoryStatus inventoryStatus,
                             PaymentStatus paymentStatus) {
        orderRecord.setOrderStatus(orderStatus);
        orderRecord.setInventoryStatus(inventoryStatus);  // know this because it will only receive a payment event from this topic if the inventory was successful
        orderRecord.setPaymentStatus(paymentStatus);
    }

    private OrderCallbackDTO constructCallbackDTO(OrderEntity orderRecord) {
        return new OrderCallbackDTO(orderRecord.getOrderID(), orderRecord.getOrderStatus(),
                orderRecord.getInventoryStatus(), orderRecord.getPaymentStatus());
    }

    public List<OrderEntity> getAllOrders(){
        return orderRepo.findAll();
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
}
