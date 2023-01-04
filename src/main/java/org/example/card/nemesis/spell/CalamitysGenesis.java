package org.example.card.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.nemesis.follow.MagisterialDreadnought;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
public class CalamitysGenesis extends SpellCard {
    public Integer cost = 1;
    public String name = "灾祸模式";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("灾厄");
    public String mark = """
        将2张正义暴君洗入牌堆。回合结束时，搜索1张5费以上随从。
        """;

    public String subMark = "";

    public CalamitysGenesis() {

        setPlay(new Play(() -> {
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(MagisterialDreadnought.class));
                addCards.add(createCard(MagisterialDreadnought.class));
                ownerPlayer().addDeck(addCards);

                // 创建主战者回合结束效果
                ownerPlayer().getLeader().addEffect(
                    new Effect(this,null,EffectTiming.EndTurn, 1,
                        obj -> ownerPlayer().draw(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                    ), true);
        }));
    }
}
