package org.example.user.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CollectionOverviewResponse {
    Integer tickets;
    Integer arcaneDust;
    List<CollectionCardResponse> cards;
}
