package com.distalgo.saga.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryTransaction {
    @Id
    private Integer orderID;
    private Integer eventID;
    private Integer seats;
    // used to have idempotent operations so can retry in case of failure????
    // e.g. !paymentRepository.existsByOrderIdAndCompensatedTrue(order.getId()) --> have to see if its sensitive to caps
//    private Boolean compensated;
}
