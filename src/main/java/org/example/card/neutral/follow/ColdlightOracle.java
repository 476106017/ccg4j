package org.example.card.neutral.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ColdlightOracle extends FollowCard {
    private String name = "寒光智者";
    private Integer cost = 3;
    private int atk = 1;
    private int hp = 2;
    private String job = "中立";
    private List<String> race = List.of("鱼人");
    private String mark = """
        战吼：每个玩家抽2张牌
        """;
    private String subMark = "";

    public ColdlightOracle() {
        super();
        getPlays().add(new Card.Event.Play(ArrayList::new,0,
            gameObjs -> {
                ownerPlayer().draw(2);
                enemyPlayer().draw(2);
            }));
    }
}
