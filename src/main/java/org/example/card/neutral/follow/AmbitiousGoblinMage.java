package org.example.card.neutral.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.GameObj;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AmbitiousGoblinMage  extends FollowCard {
    private String name = "宏愿哥布林法师";
    private Integer cost = 1;
    private int atk = 2;
    private int hp = 1;
    private String job = "中立";
    private List<String> race = List.of("人类");
    private String mark = """
        战吼：将手牌的1张随从牌放回牌堆，抽1张费用低于该牌的随从牌
        """;
    private String subMark = "";

    public AmbitiousGoblinMage() {
        super();
        getPlays().add(new Card.Event.Play(
            ()->ownerPlayer().getHand().stream()
                .filter(card -> card instanceof FollowCard).map(card -> (GameObj)card).toList(),
            1,
            target->{
                ownerPlayer().back((FollowCard)target.get(0));
                ownerPlayer().draw(card -> card instanceof FollowCard followCard && followCard.getCost() < getCost());
            }));

    }
}