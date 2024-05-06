package com.distalgo.saga.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTransaction {
    @Id
    private Integer orderID;
    private Integer userID;
    private Integer amount;
    // used to have idempotent operations so can retry in case of failure????
    // e.g. !paymentRepository.existsByOrderIdAndCompensatedTrue(order.getId()) --> have to see if its sensitive to caps
//    private Boolean refunded;
}
