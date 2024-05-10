# Ticketing System using the Saga Pattern

A ticketing system implemented with the Saga Pattern.

## Overall Architecture

![Implementation Architecture](https://github.com/mwwl/ticketingsystem/blob/60e556347ea0131e51426367c4c560f2d6a9ad92/SAGA_architecture.png)


## Microservices Involved

### Order Service
- Receives a client request from the client through the `client-event` topic in Kafka. 
- Creates an order event, and publishes it to the `order-event` topic.
- Also consumes events from the `order-updates` topic, from either the inventory service or the payment service.

### Inventory Service
- Consumes the order event from the `order-event` topic, and creates an inventory event.
- If the inventory check is successful, it creates a payment event, and publishes it to the `inventory-event` topic.
- If it fails, it sends the inventory event to the `order-updates` topic for the order service to consume.

### Payment Service
- Consumes an inventory event from the `inventory-event` topic, and creates a payment event.
- It verifies the users' account balance, and publishes the payment event to the `order-updates` topic.





## Starting the Application
Before running the application, in the terminal:
1. Install Apache Kafka
    ```shell
    brew install kafka
    ```
2. Start Zookeeper
    ```shell
    ~/kafka_2.12-3.7.0/bin/zookeeper-server-start.sh ~/kafka_2.12-3.7.0/config/zookeeper.properties
    ```
3. Start Apache Kafka
    ```shell
    ~/kafka_2.12-3.7.0/bin/kafka-server-start.sh ~/kafka_2.12-3.7.0/config/server.properties
    ```

After ensuring the above is functioning:
1. Run ClientApplication
    - Remember to specify the IP Address and Port Number for the client

2. Run OrderServiceApplication
3. Run InventoryServiceApplication
4. Run PaymentServiceApplication

## File Directory Tree
```
ticketingsystem/
├── .mvn
├── client-new/
│   ├── src/main/
│   │   ├── java/com/distalgo/client/
│   │   │   ├── config/
│   │   │   │   ├── ClientConsumerConfig.java
│   │   │   │   └── ClientPublisherConfig.java
│   │   │   ├── gui/
│   │   │   │   ├── ClientGUI.form
│   │   │   │   └── ClientGUI.java
│   │   │   ├── service/
│   │   │   │   ├── ClientConsumerService.java
│   │   │   │   ├── ClientPublisherService.java
│   │   │   │   └── ClientService.java
│   │   │   └── ClientApplication.java
│   │   └── resources
│   └── pom.xml
├── common/
│   ├── src/main/java/com/distalgo/saga/
│   │   ├── callback/
│   │   │   ├── CallbackEntity.java
│   │   │   ├── CallbackRepo.java
│   │   │   └── CallbackService.java
│   │   ├── dto/
│   │   │   ├── ClientRequestDTO.java
│   │   │   ├── InventoryRequestDTO.java
│   │   │   ├── OrderCallbackDTO.java
│   │   │   ├── OrderRequestDTO.java
│   │   │   └── PaymentRequestDTO.java
│   │   └── events/
│   │       ├── CallbackEvent.java
│   │       ├── ClientEvent.java
│   │       ├── Event.java
│   │       ├── InventoryEvent.java
│   │       ├── InventoryStatus.java
│   │       ├── OrderEvent.java
│   │       ├── OrderStatus.java
│   │       ├── PaymentEvent.java
│   │       └── PaymentStatus.java
│   └── pom.xml
├── inventory-service/
│   ├── src/main/
│   │   ├── java/com/distalgo/saga/inventory/
│   │   │   ├── config/
│   │   │   │   ├── InventoryConsumerConfig.java
│   │   │   │   └── InventoryPublisherConfig.java
│   │   │   ├── entity/
│   │   │   │   ├── EventInventory.java
│   │   │   │   └── InventoryTransaction.java
│   │   │   ├── repo/
│   │   │   │   ├── EventInventoryRepo.java
│   │   │   │   └── InventoryTransactionRepo.java
│   │   │   └── service/
│   │   │       ├── InventoryConsumerService.java
│   │   │       ├── InventoryEventPublisher.java
│   │   │       └── InventoryValidationService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application.yml
│   └── pom.xml
├── order-service/
│   ├── src/main/
│   │   ├── java/com/distalgo/saga/order/
│   │   │   ├── config/
│   │   │   │   ├── OrderConsumerConfig.java
│   │   │   │   └── OrderEventPublisherConfig.java
│   │   │   ├── entity/
│   │   │   │   └── OrderEntity.java
│   │   │   ├── repo/
│   │   │   │   └── OrderRepo.java
│   │   │   ├── service/
│   │   │   │   ├── OrderConsumerService.java
│   │   │   │   ├── OrderPublisherService.java
│   │   │   │   └── OrderService.java
│   │   │   └── OrderServiceApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application.yml
│   └── pom.xml
├── payment-service/
│   ├── src/main/
│   │   ├── java/com/distalgo/saga/payment/
│   │   │   ├── config/
│   │   │   │   ├── PaymentConsumerConfig.java
│   │   │   │   └── PaymentPublisherConfig.java
│   │   │   ├── entity/
│   │   │   │   ├── UserBalance.java
│   │   │   │   └── UserTransaction.java
│   │   │   ├── repo/
│   │   │   │   ├── UserBalanceRepo.java
│   │   │   │   └── UserTransactionRepo.java
│   │   │   ├── service/
│   │   │   │   ├── PaymentConsumerService.java
│   │   │   │   ├── PaymentEventPublisher.java
│   │   │   │   └── PaymentValidationService.java
│   │   │   └── PaymentServiceApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application.yml
│   └── pom.xml
├── .gitignore
├── Functions Overview.docx
├── mvnw
├── mvnw.cmd
├── README.md
└── pom.xml
```
