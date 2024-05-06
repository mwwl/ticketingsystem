package com.distalgo.client.controller;


import com.distalgo.client.gui.ClientGUI;
import com.distalgo.saga.dto.OrderCallbackDTO;
import com.distalgo.saga.events.InventoryStatus;
import com.distalgo.saga.events.OrderStatus;
import com.distalgo.saga.events.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static java.lang.String.format;

/**
 * Primarily for callbacks -- order response to the client, so that the user is updated on what is happening LOL
 */
@RestController
public class ClientController {

    private ClientGUI clientGUI;

//    @Autowired
//    private OrderController orderController;

    /**
     * OrderCallbackDTO includes orderID, order status, inventory status, payment status
     * <p>
     * This is what is received
     * <p>
     * Is post mapping
     *
     * @param orderCallbackDTO
     */
    @PostMapping("/callback/{sessionID}")
    private void receiveCallback(@PathVariable("sessionID") String sessionID, @RequestBody OrderCallbackDTO orderCallbackDTO) {
        Integer orderID = orderCallbackDTO.getOrderID();
        OrderStatus orderStatus = orderCallbackDTO.getOrderStatus();
        InventoryStatus inventoryStatus = orderCallbackDTO.getInventoryStatus();
        PaymentStatus paymentStatus = orderCallbackDTO.getPaymentStatus();
        clientGUI.updateOutputBox(orderID, orderStatus, inventoryStatus, paymentStatus);
    }

    /**
     * Sends the order request to the order microservice together with the session ID
     */
    public void sendOrderRequest(String sessionID, ArrayList<Integer> input, String clientIP, String clientPort) {
        System.out.println("SESSION ID: " + sessionID);
        String baseURL = "http://localhost:8081/order";

        WebClient webClient = WebClient.create(baseURL);
        String requestBody = createRequestBody(input);

        System.out.println("request body created: " + requestBody);

        // Sends data to the server
        assert requestBody != null;
        Mono<Void> resultMono = webClient.post()
                .uri("/create/{sessionID}/{clientIP}/{clientPort}", sessionID, clientIP, clientPort)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class); // if there are no response body

        resultMono.subscribe(
                result -> System.out.println("Request sent successfully: " + requestBody),
                error -> System.err.println("Error sending request: " + error),
                () -> System.out.println("Completed")
        );
    }

    /**
     * Creating the request body in a json format
     *
     * @param input
     * @return
     */
    private String createRequestBody(ArrayList<Integer> input) {
        Integer userID = input.get(0);
        Integer eventID = input.get(1);
        Integer seats = input.get(2);

        return format("{\"userID\": %d, \"ticketedEventID\": %d, \"seats\": %d, \"amount\": %d}",
                userID, eventID, seats, seats * 10);
    }
}
