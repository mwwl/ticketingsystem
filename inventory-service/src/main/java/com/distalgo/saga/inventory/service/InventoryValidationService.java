package com.distalgo.saga.inventory.service;

import com.distalgo.saga.dto.InventoryRequestDTO;
import com.distalgo.saga.dto.OrderRequestDTO;
import com.distalgo.saga.events.InventoryEvent;
import com.distalgo.saga.events.InventoryStatus;
import com.distalgo.saga.events.OrderEvent;
import com.distalgo.saga.inventory.entity.InventoryTransaction;
import com.distalgo.saga.inventory.repo.EventInventoryRepo;
import com.distalgo.saga.inventory.repo.InventoryTransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Creates a new Inventory Event (if the order has been created) -- within which, validates whether there is
 * enough inventory or not
 *
 * Alternatively, cancels the already existing order-event if there has been a cancellation of the order
 */
@Service
public class InventoryValidationService {
    private static final int USER_ID = 0;
    private static final int EVENT_ID = 1;
    private static final int SEATS = 2;
    private static final int AMOUNT = 3;
    private static final int ORDER_ID = 4;

    @Autowired
    private EventInventoryRepo eventInventoryRepo;

    @Autowired
    private InventoryTransactionRepo inventoryTransactionRepo;

    /**
     * The order has been created.
     *
     * Now need to check against the inventory if there's enough inventory left for the
     * order to proceed or not, if it doesn't the inventory status will be FAILED.
     *
     * Also, if there is no such event (that the user wants to reserve), the inventory status will reflect that
     *
     * @param orderEvent
     * @return
     */
    @Transactional
    public InventoryEvent newOrderEvent(OrderEvent orderEvent) {
        ArrayList<Integer> details = retrieveDetails(orderEvent);
        Integer userID = details.get(USER_ID);
        Integer eventID = details.get(EVENT_ID);
        Integer seats = details.get(SEATS);
        Integer amount = details.get(AMOUNT);
        Integer orderID = details.get(ORDER_ID);

        InventoryRequestDTO inventoryRequestDTO = new InventoryRequestDTO(userID, eventID, seats, amount, orderID);

        // how to raise a flag if there is no such event ID??
        return eventInventoryRepo.findById(eventID)
                .map(inventoryBalance -> {
                    if (inventoryBalance.getSeatsAvail() >= seats) {
                        // there is enough seats to be booked
                        inventoryBalance.setSeatsAvail(inventoryBalance.getSeatsAvail() - seats);
                        System.out.println("==== DEDUCTED FROM INVENTORY ====");
                        inventoryTransactionRepo.save(new InventoryTransaction(orderID, eventID, seats));
                        return new InventoryEvent(inventoryRequestDTO, InventoryStatus.INVENTORY_CHECK_SUCCESS);
                    } else {
                        return new InventoryEvent(inventoryRequestDTO, InventoryStatus.INVENTORY_CHECK_FAILED);
                    }
                })
                .orElse(new InventoryEvent(inventoryRequestDTO, InventoryStatus.NO_EVENT));
        // TODO: Have to communicate this with the client when no event has been found
    }

    /**
     * Order has been cancelled (for 1 reason or another, could be the user cancelling it, or whatever)
     *
     * Have to delete the transaction from the InventoryTransaction repo,
     * and add the inventory back to the EventInventory repo, and update the transaction that its
     *
     * This is the compensation part of it
     *
     * This is an idempotent operation as after its been executed once, the record would have gotten deleted from the
     * transaction repository. SO, if it has been deleted, they won't be able to find it again, and so the inventory
     * won't get updated/added to again
     *
     * @param orderEvent
     */
    @Transactional
    public void cancelOrderEvent(OrderEvent orderEvent) {
        ArrayList<Integer> details = retrieveDetails(orderEvent);
        Integer orderID = details.get(ORDER_ID);

        inventoryTransactionRepo.findById(orderID)
                .ifPresent(inventoryTransaction -> {
                    Integer eventID = inventoryTransaction.getEventID();
                    System.out.println("cancelling transaction for this event: " + eventID + " for this order: " + orderID);
                    Integer seatsOrdered = inventoryTransaction.getSeats();
                    inventoryTransactionRepo.delete(inventoryTransaction);
                    System.out.println("==== ADDED BACK TO INVENTORY ====");
                    eventInventoryRepo.findById(eventID)
                            .ifPresent(seatBalance -> seatBalance.setSeatsAvail(seatBalance.getSeatsAvail() + seatsOrdered));
                });
    }

    /**
     * Details used to build the inventoryRequestDTO
     *
     * @param orderEvent
     * @return
     */
    private ArrayList<Integer> retrieveDetails(OrderEvent orderEvent) {
        ArrayList<Integer> details = new ArrayList<>();
        OrderRequestDTO orderRequestDTO = orderEvent.getOrderRequestDTO();

        details.add(orderRequestDTO.getUserID());
        details.add(orderRequestDTO.getTicketedEventID());
        details.add(orderRequestDTO.getSeats());
        details.add(orderRequestDTO.getAmount());
        details.add(orderRequestDTO.getOrderID());
        return details;
    }
}
