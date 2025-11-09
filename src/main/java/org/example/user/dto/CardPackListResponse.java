package org.example.user.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CardPackListResponse {
    Integer tickets;
    List<CardPackView> packs;
}
