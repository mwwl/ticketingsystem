package com.distalgo.client.service;

import com.distalgo.client.gui.ClientGUI;
import com.distalgo.saga.dto.OrderCallbackDTO;
import com.distalgo.saga.events.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Scope("singleton")
public class ClientService {
    private ClientGUI clientGUI;
    private String sessionID;
    private Instant latestPublishedTimestamp = null;

    public void launchGUI(String sessionID, ClientPublisherService publisherService) {
        System.out.println("in launch gui - client service: " + publisherService);
        System.out.println("the client GUI object (before launching): " + this.clientGUI);
        this.clientGUI = new ClientGUI(sessionID, publisherService);
        System.out.println("the client GUI object (after launching): " + this.clientGUI);
        System.out.println("ClientService instance hashcode in launch gui: " + this.hashCode());
    }

    // callback event is received, within which, there is the callbackDTO, and that is where the status stuff is -- can call the updateGUI method in clientGUI
    public void updateClient(CallbackEvent callbackEvent) {
        OrderCallbackDTO orderCallbackDTO = callbackEvent.getOrderCallbackDTO();

        Integer orderID = orderCallbackDTO.getOrderID();
        OrderStatus orderStatus = orderCallbackDTO.getOrderStatus();
        InventoryStatus inventoryStatus = orderCallbackDTO.getInventoryStatus();
        PaymentStatus paymentStatus = orderCallbackDTO.getPaymentStatus();

        System.out.println("sending to output box to update the GUI");
        System.out.println("client GUI object (in updateClient): " + this.clientGUI);
        System.out.println("ClientService instance hashcode (in updateClient): " + this.hashCode());
        clientGUI.updateOutputBox(orderID, orderStatus, inventoryStatus, paymentStatus);
    }

    public void updateClient(String update) {
        System.out.println("going to update client: " + update);
        clientGUI.updateOutputBox(update);
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public void saveCurrentTime(Instant time) {
        System.out.println("current time saved: " + time);
        latestPublishedTimestamp = time;
    }

    public Instant getLastPublishedTime() {
        return latestPublishedTimestamp;
    }
}
