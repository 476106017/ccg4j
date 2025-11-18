package org.example.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.card.Card;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderStatusInfo {
    private String type;          // status / effect
    private String key;
    private String label;
    private String description;
    private Integer value;
    private Card card;

    public static LeaderStatusInfo status(String key, String label, String description, int value) {
        return LeaderStatusInfo.builder()
            .type("status")
            .key(key)
            .label(label)
            .description(description)
            .value(value)
            .build();
    }

    public static LeaderStatusInfo effect(Card card) {
        return LeaderStatusInfo.builder()
            .type("effect")
            .label(card.getName())
            .card(card)
            .build();
    }
}
