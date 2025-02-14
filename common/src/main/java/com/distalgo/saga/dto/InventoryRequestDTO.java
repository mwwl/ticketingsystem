package com.distalgo.saga.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequestDTO {
    private Integer userID; // used to validate the available amount left for the user in payment service
    private Integer ticketedEventID; // used to validate seats left for the event in inventory service
    private Integer seats; // number of seats to reserve
    private Integer amount; // total amount, cash-wise
    private Integer orderID; // autogenerated through the system
}
