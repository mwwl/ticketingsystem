package com.distalgo.saga.events;

import com.distalgo.saga.dto.OrderRequestDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class OrderEvent implements Event{
        // deleted @JsonProperty("____")
        private final UUID eventID = UUID.randomUUID();
        private final Date date = new Date();
        private OrderRequestDTO orderRequestDTO;
        private OrderStatus orderStatus;

        public OrderEvent(OrderRequestDTO orderRequestDTO, OrderStatus orderStatus) {
            this.orderRequestDTO = orderRequestDTO;
            this.orderStatus = orderStatus;
        }

        /**
         * Gets the event ID of the current order event
         *
         * @return the unique event ID
         */
        @Override
        public UUID getEventID() {
            return eventID;
        }

        /**
         * Gets the date the order event was created
         * @return date
         */
        @Override
        public Date getDate() {
            return date;
        }

}


