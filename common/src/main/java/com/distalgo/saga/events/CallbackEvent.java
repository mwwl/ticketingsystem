package com.distalgo.saga.events;

import com.distalgo.saga.dto.OrderCallbackDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.ast.Call;

import javax.security.auth.callback.Callback;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CallbackEvent implements Event {
    private final UUID eventID = UUID.randomUUID();
    private final Date date = new Date();
    private String sessionID;
    private OrderCallbackDTO orderCallbackDTO;

    public CallbackEvent (OrderCallbackDTO orderCallbackDTO) {
        this.orderCallbackDTO = orderCallbackDTO;
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
