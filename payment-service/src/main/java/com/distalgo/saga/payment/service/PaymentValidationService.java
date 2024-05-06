package com.distalgo.saga.payment.service;

import com.distalgo.saga.dto.InventoryRequestDTO;
import com.distalgo.saga.dto.PaymentRequestDTO;
import com.distalgo.saga.events.InventoryEvent;
import com.distalgo.saga.events.PaymentEvent;
import com.distalgo.saga.events.PaymentStatus;
import com.distalgo.saga.payment.entity.UserTransaction;
import com.distalgo.saga.payment.repo.UserBalanceRepo;
import com.distalgo.saga.payment.repo.UserTransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class PaymentValidationService {
    private static final int USER_ID = 0;
    private static final int AMOUNT = 3;
    private static final int ORDER_ID = 4;

    @Autowired
    private UserBalanceRepo userBalanceRepo;

    @Autowired
    private UserTransactionRepo userTransactionRepo;

    /**
     * The inventory check has been successful, now need to check whether the user
     * has enough balance left for the order to be completed or not.
     *
     * If there is enough balance, the payment status will be SUCCESS. Otherwise, it
     * will be FAILED
     *
     * If there is no such user, then it will reflect that (but should this be done earlier though??)
     *
     * Made it transactional so that this whole method executes together as a transaction
     *
     */
    @Transactional
    public PaymentEvent newInventoryEvent(InventoryEvent inventoryEvent) {
        ArrayList<Integer> details = retrieveDetails(inventoryEvent);
        Integer userID = details.get(USER_ID);
        Integer amount = details.get(AMOUNT);
        Integer orderID = details.get(ORDER_ID);

        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO(userID, amount, orderID);

        return userBalanceRepo.findById(userID)
                .map(userBalance -> {
                    if (userBalance.getBalance() >= amount) {
                        userBalance.setBalance(userBalance.getBalance() - amount);
                        userTransactionRepo.save(new UserTransaction(orderID, userID, amount));
                        return new PaymentEvent(paymentRequestDTO, PaymentStatus.PAYMENT_SUCCESS);
                    } else {
                        return new PaymentEvent(paymentRequestDTO, PaymentStatus.PAYMENT_FAILED);
                    }
                })
                .orElse(new PaymentEvent(paymentRequestDTO, PaymentStatus.USER_NOT_FOUND));
        // TODO: Have to communicate this with the client the user is not in the db
    }

    /**
     * Order has been cancelled, inventory has been cancelled, now its time for the payment to be cancelled and refunded
     * Have to update in the repo (user transaction repo) the refunded status if its done
     */
    @Transactional
    public void cancelInventoryEvent(InventoryEvent inventoryEvent) {
        ArrayList<Integer> details = retrieveDetails(inventoryEvent);
        Integer orderID = details.get(ORDER_ID);

        userTransactionRepo.findById(orderID)
                .ifPresent(userTransaction -> {
                    Integer userID = userTransaction.getUserID();
                    System.out.println("cancelling payment for this user: " + userID + "for this order: " + orderID);
                    Integer amountToRefund = userTransaction.getAmount();
                    userTransactionRepo.delete(userTransaction);
                    userBalanceRepo.findById(userID)
                            .ifPresent(userBalance -> userBalance.setBalance(userBalance.getBalance() + amountToRefund));
                });
    }



    /**
     * Details used to build the inventoryRequestDTO
     *
     * @param inventoryEvent
     * @return
     */
    private ArrayList<Integer> retrieveDetails(InventoryEvent inventoryEvent) {
        ArrayList<Integer> details = new ArrayList<>();
        InventoryRequestDTO inventoryRequestDTO = inventoryEvent.getInventoryRequestDTO();

        details.add(inventoryRequestDTO.getUserID());
        details.add(inventoryRequestDTO.getTicketedEventID());
        details.add(inventoryRequestDTO.getSeats());
        details.add(inventoryRequestDTO.getAmount());
        details.add(inventoryRequestDTO.getOrderID());
        return details;
    }





}
