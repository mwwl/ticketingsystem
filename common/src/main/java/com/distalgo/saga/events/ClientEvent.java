package com.distalgo.saga.events;


import com.distalgo.saga.dto.ClientRequestDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ClientEvent implements Event {

    private final UUID eventID = UUID.randomUUID();
    private final Date date = new Date();
    private ClientRequestDTO clientRequestDTO;

    public ClientEvent(ClientRequestDTO clientRequestDTO) {
        this.clientRequestDTO = clientRequestDTO;
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
