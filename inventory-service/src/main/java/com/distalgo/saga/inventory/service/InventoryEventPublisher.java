package com.distalgo.saga.inventory.service;

import com.distalgo.saga.events.InventoryEvent;
import com.distalgo.saga.events.InventoryStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventPublisher {
    private final ReactiveKafkaProducerTemplate<String, InventoryEvent> inventoryEventProducerKafkaTemplate;

    @Value(value = "order-updates")
    private String failedTopic;

    @Value(value = "inventory-event")
    private String successTopic;

    public InventoryEventPublisher(ReactiveKafkaProducerTemplate<String, InventoryEvent> inventoryEventProducerKafkaTemplate) {
        this.inventoryEventProducerKafkaTemplate = inventoryEventProducerKafkaTemplate;
    }

    /**
     * Publishes the inventory event to the respective channels, depending on the status of the inventory check
     * If inventory check succeeds, sends to the inventory-event topic for payment service to consume
     * If inventory check fails, sends to the order-updates topic for the order service to consume
     */
    public void publishInventoryEvent(InventoryEvent inventoryEvent) {
        if (inventoryEvent.getInventoryStatus().equals(InventoryStatus.INVENTORY_CHECK_SUCCESS)) {
            inventoryEventProducerKafkaTemplate.send(successTopic, inventoryEvent)
                    .doOnSuccess(sendResult -> System.out.println("sent to success (inventory-event): " + inventoryEvent))
                    .subscribe();
        } else {
            inventoryEventProducerKafkaTemplate.send(failedTopic, inventoryEvent)
                    .doOnSuccess(sendResult -> System.out.println("sent to failed (order-updates): " + inventoryEvent))
                    .subscribe();
        }
    }
}
