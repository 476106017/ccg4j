package org.example.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeckResponse {
    Long id;
    String deckName;
    String deckData;
    Integer totalDust;  // 总尘数
    Integer cardCount;  // 卡牌数量
    String createdAt;
    String updatedAt;
}
