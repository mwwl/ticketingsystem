package com.distalgo.saga.callback;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallbackEntity {
    @Id
    String sessionID;
    String callbackURL;
}
