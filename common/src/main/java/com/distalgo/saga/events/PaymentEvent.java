package com.distalgo.saga.events;

import com.distalgo.saga.dto.PaymentRequestDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PaymentEvent implements Event, Serializable {
    private UUID eventID = UUID.randomUUID();
    private Date data = new Date();
    private PaymentRequestDTO paymentRequestDTO;
    private PaymentStatus paymentStatus;

    public PaymentEvent(PaymentRequestDTO paymentRequestDTO, PaymentStatus paymentStatus) {
        this.paymentRequestDTO = paymentRequestDTO;
        this.paymentStatus = paymentStatus;
    }

    @Override
    public UUID getEventID() {
        return null;
    }

    @Override
    public Date getDate() {
        return null;
    }
}
