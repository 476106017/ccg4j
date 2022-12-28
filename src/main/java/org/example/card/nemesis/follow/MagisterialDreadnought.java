package org.example.card.nemesis.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class MagisterialDreadnought extends FollowCard {
    private Integer cost = 5;
    private String name = "正义暴君";
    private String job = "复仇者";

    private List<String> race = new ArrayList<>();
    private String mark = """
        瞬念召唤：回合开始时被破坏的5费以上随从大于5个
        入场时：召唤1个世界驱除者
        离场时：将2张幻境粉碎者洗入牌堆
        """;
    private String subMark = "";

    private int atk = 5;
    private int hp = 5;

    public MagisterialDreadnought() {
        setMaxHp(getHp());
        getEnterings().add(new AreaCard.Event.Entering(()->{
            ownerPlayer().summon(createCard(WorldEliminator.class));
        }));
        getLeavings().add(new AreaCard.Event.Leaving(()->{
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
