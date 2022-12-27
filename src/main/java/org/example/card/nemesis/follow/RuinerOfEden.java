package org.example.card.nemesis.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.AreaCard;
import org.example.card.nemesis.spell.CalamitysEnd;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class RuinerOfEden extends FollowCard {
    private Integer cost = 8;
    private String name = "幻境粉碎者";
    private String job = "复仇者";

    private List<String> race = new ArrayList<>();
    private String mark = """
        瞬念召唤：回合开始时被破坏的5费以上随从大于10个
        入场时：召唤3个世界驱除者，并赋予【剧毒】
        离场时：将1张灾祸降临洗入牌堆
        """;
    private String subMark = "";

    private int atk = 5;
    private int hp = 5;

    public RuinerOfEden() {
        super();
        getKeywords().add("守护");
        getKeywords().add("剧毒");
        getEnterings().add(new AreaCard.Event.Entering(()->{
            ownerPlayer().summon(createCard(WorldEliminator.class,"剧毒"));
            ownerPlayer().summon(createCard(WorldEliminator.class,"剧毒"));
            ownerPlayer().summon(createCard(WorldEliminator.class,"剧毒"));
        }));
        getLeavings().add(new AreaCard.Event.Leaving(()->{
            List<Card> addCards = new ArrayList<>();
            addCards.add(createCard(CalamitysEnd.class));
            ownerPlayer().addDeck(addCards);
        }));
        getInvocationBegins().add(new Card.Event.InvocationBegin(
            ()-> ownerPlayer().getGraveyard().stream()
                .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                .count() >= 10,
            ()->{}
        ));
    }

}
