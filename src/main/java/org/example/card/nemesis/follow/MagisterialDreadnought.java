package org.example.card.nemesis.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class MagisterialDreadnought extends FollowCard {
    private Integer cost = 5;
    private String name = "正义暴君";
    private String job = "复仇者";

    private List<String> race = new ArrayList<>();
    private String mark = """
        瞬念召唤：回合开始时被破坏的5费以上随从大于5个
        入场时：召唤1个世界驱除者
        离场时：增加2张幻境粉碎者到牌堆中
        """;
    private String subMark = "";

    private int atk = 5;
    private int hp = 5;
    private int maxHp = 5;

    public MagisterialDreadnought() {
        getEnterings().add(new Event.Entering(()->{
            ownerPlayer().summon(createCard(WorldEliminator.class));
        }));
        getLeavings().add(new Event.Leaving(()->{
            List<Card> addCards = new ArrayList<>();
            addCards.add(createCard(RuinerOfEden.class));
            addCards.add(createCard(RuinerOfEden.class));
            ownerPlayer().addDeck(addCards);
        }));
        getInvocationBegins().add(new Card.Event.InvocationBegin(
            ()-> ownerPlayer().getGraveyard().stream()
                .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                .count() >= 5,
            ()->{}
        ));
    }

}
