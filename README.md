# Ticketing System using the Saga Pattern

A ticketing system implemented with the Saga Pattern.

[Insert architecture]

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
