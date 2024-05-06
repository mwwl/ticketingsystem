package com.distalgo.saga.events;

import com.distalgo.saga.dto.InventoryRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class InventoryEvent implements Event{
    private final UUID eventID = UUID.randomUUID();
    private final Date date = new Date();
    private InventoryRequestDTO inventoryRequestDTO;
    private InventoryStatus inventoryStatus;

    public InventoryEvent(InventoryRequestDTO inventoryRequestDTO, InventoryStatus inventoryStatus) {
        this.inventoryRequestDTO = inventoryRequestDTO;
        this.inventoryStatus = inventoryStatus;
    }

    @Override
    public UUID getEventID() {
        return eventID;
    }

    @Override
    public Date getDate() {
        return date;
    }
}
