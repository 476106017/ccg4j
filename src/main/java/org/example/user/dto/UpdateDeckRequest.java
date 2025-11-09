package org.example.user.dto;

import lombok.Data;

@Data
public class UpdateDeckRequest {
    private String deckName;
    private String deckData;
}
