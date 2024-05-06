package com.distalgo.saga.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientRequestDTO {
    private String sessionID;
    private Integer userID;
    private Integer eventID;
    private Integer seats;
}
