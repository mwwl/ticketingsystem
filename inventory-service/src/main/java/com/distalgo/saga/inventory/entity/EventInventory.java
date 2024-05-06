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
public class EventInventory {
    @Id
    private Integer eventID;
    private Integer seatsAvail;
}
