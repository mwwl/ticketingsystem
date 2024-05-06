package com.distalgo.saga.order.controller;

import com.distalgo.saga.callback.CallbackService;
import com.distalgo.saga.dto.OrderCallbackDTO;
import com.distalgo.saga.dto.OrderRequestDTO;
import com.distalgo.saga.order.entity.OrderEntity;
import com.distalgo.saga.order.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired // to inject it, so we can directly return in the below method
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private String sessionID;

//    @Autowired
//    private CallbackService callbackService;

    /**
     * Returns the entity (writing the endpoint)
     *
     * Receives the request from the client (from the GUI) and takes the input as a order request
     *
     * Need to annotate it as a request body because this is the post mapping
     */
    @PostMapping("/create")
    public OrderEntity createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {

        this.sessionID = sessionID;
//        String clientIP = request.getRemoteAddr();
//        Integer clientPort = request.getLocalPort();
//        this.orderCallbackService = orderCallbackService;
        // call the service to create that order
        System.out.println("Post mapping, /create");
        System.out.println("request body: " + orderRequestDTO);
        // request body: OrderRequestDTO(userID=106, ticketedEventID=10500, seats=1000, amount=10000, orderID=null)
        OrderEntity order =  orderService.createOrder(orderRequestDTO);
        System.out.println("order: " + order);
        // order: OrderEntity(orderID=852, userID=106, eventID=10500, price=10000, seats=1000, orderStatus=ORDER_CREATED, inventoryStatus=null, paymentStatus=null)
        return order;
    }

    /**
     * Another endpoint to retrieve all ticket orders - when doing GET, use the same url as the "larger" class (aka /order)
     */
    @GetMapping
    public List<OrderEntity> getOrders() {
        return orderService.getAllOrders();
    }
//
//    /**
//     * Sends the status of the order back to the client -- need to extract from the order entity
//     */
    public void sendOrderStatus(String sessionID, OrderCallbackDTO orderCallbackDTO, String clientIP, String clientPort) {
//        OrderCallbackService orderCallbackService = OrderCallbackService.getInstance();
//        System.out.println("hashmap in order controller: " + orderCallbackService.getHashMap());
        CallbackService callbackService = new CallbackService();
        System.out.println("in order controller, going to send order status back to client");
        String callbackURL = createCallbackURL(sessionID, clientIP, clientPort);
        System.out.println("callbackurl: " + callbackURL);
        WebClient webClient = WebClient.create();

        try {
            String requestBody = objectMapper.writeValueAsString(orderCallbackDTO);

            Mono<Void> resultMono = webClient.post()
                    .uri(callbackURL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Void.class);

            resultMono.subscribe(
                    result -> System.out.println("Request sent successfully to client: " + requestBody),
                    error -> System.err.println("Error sending request to client: " + error),
                    () -> System.out.println("Completed")
            );
        } catch (JsonProcessingException | WebClientException e) {
            throw new RuntimeException(e);
        }
    }

    private String createCallbackURL(String sessionID, String clientIP, String clientPort) {
        return String.format("http://%s:%s/callback/%s", clientIP, clientPort, sessionID);
    }
}
