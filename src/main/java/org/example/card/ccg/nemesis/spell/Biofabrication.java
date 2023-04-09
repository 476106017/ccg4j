package org.example.card.ccg.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.ccg.fairy.spell.ForestGenesis;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class Biofabrication extends SpellCard {
    public Integer cost = 0;
    public String name = "生命量产";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("");
    public String mark = """
        使1张手牌中的创造物卡消费-1。
        增加3张与该创造物卡同名的卡片到牌堆中。
        """;

    public String subMark = "";

    public void init() {

        setPlay(new Play(
            ()->ownerPlayer().getHandAsGameObjBy(followCard -> followCard.hasRace("创造物")),
            true,
            target->{
                final Card card = (Card) target;
                card.addCost(-1);

                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(card.getClass()));
                addCards.add(createCard(card.getClass()));
                addCards.add(createCard(card.getClass()));
                ownerPlayer().addDeck(addCards);
            }));
    }

}
