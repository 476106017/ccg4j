package org.example.user.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CollectionCardResponse {
    String code;
    String name;
    String type;
    Integer cost;
    String job;
    List<String> race;
    List<String> keywords;
    String mark;
    String rarity;
    Integer quantity;
    Integer atk;
    Integer hp;
}
