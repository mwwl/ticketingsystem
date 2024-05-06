package com.distalgo.saga.order.entity;

import com.distalgo.saga.events.InventoryStatus;
import com.distalgo.saga.events.OrderStatus;
import com.distalgo.saga.events.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ORDERS_TBL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntity {
    @Id
    @GeneratedValue
    private Integer orderID;
    private Integer userID;
    private String sessionID;

    private Integer eventID;
    private Integer price;
    private Integer seats;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private InventoryStatus inventoryStatus;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}
