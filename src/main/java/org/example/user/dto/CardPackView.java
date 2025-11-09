package org.example.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CardPackView {
    String code;
    String name;
    String description;
}
