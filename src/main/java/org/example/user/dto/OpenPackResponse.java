package org.example.user.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class OpenPackResponse {
    Integer remainingTickets;
    List<CollectionCardResponse> cards;
}
