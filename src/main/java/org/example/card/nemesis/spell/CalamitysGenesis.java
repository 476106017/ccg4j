package org.example.card.nemesis.spell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.nemesis.follow.MagisterialDreadnought;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Leader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@EqualsAndHashCode(callSuper = true)
@Data
public class CalamitysGenesis extends SpellCard {
    public Integer cost = 1;
    public String name = "灾祸模式";
    public String job = "复仇者";
    private List<String> race = List.of("灾厄");
    public String mark = """
        增加2张正义暴君到牌堆中。回合结束时，抽1张5费以上随从。
        """;

    public String subMark = "";

    public CalamitysGenesis() {

        getPlays().add(new Card.Event.Play(ArrayList::new,0,
            gameObjs -> {
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(MagisterialDreadnought.class));
                addCards.add(createCard(MagisterialDreadnought.class));
                ownerPlayer().addDeck(addCards);

                // 创建主战者回合结束效果
                ownerPlayer().getLeader().addEffect(this,EffectTiming.EndTurn, 1,
                    damage -> ownerPlayer().draw(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                );
        }));
    }
}
