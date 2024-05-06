package com.distalgo.client.service;

import com.distalgo.client.gui.ClientGUI;
import com.distalgo.saga.dto.OrderCallbackDTO;
import com.distalgo.saga.events.*;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
    private ClientGUI clientGUI;

    public void launchGUI(String sessionID, ClientPublisherService publisherService) {
        System.out.println("in launch gui - client service: " + publisherService);
        this.clientGUI = new ClientGUI(sessionID, publisherService);
    }

    // callback event is received, within which, there is the callbackDTO, and that is where the status stuff is -- can call the updateGUI method in clientGUI
    public void updateClient(CallbackEvent callbackEvent) {
        OrderCallbackDTO orderCallbackDTO = callbackEvent.getOrderCallbackDTO();

        Integer orderID = orderCallbackDTO.getOrderID();
        OrderStatus orderStatus = orderCallbackDTO.getOrderStatus();
        InventoryStatus inventoryStatus = orderCallbackDTO.getInventoryStatus();
        PaymentStatus paymentStatus = orderCallbackDTO.getPaymentStatus();

        System.out.println("sending to output box to update the GUI");
        clientGUI.updateOutputBox(orderID, orderStatus, inventoryStatus, paymentStatus);
    }
}
