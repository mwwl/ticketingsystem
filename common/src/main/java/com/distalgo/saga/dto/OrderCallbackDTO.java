package com.distalgo.saga.dto;

import com.distalgo.saga.events.InventoryStatus;
import com.distalgo.saga.events.OrderStatus;
import com.distalgo.saga.events.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCallbackDTO {
    private Integer orderID;
    private OrderStatus orderStatus;
    private InventoryStatus inventoryStatus;
    private PaymentStatus paymentStatus;
}
