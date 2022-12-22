package org.example.card.fairy.spell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.fairy.follow.EternalSeedling;
import org.example.card.nemesis.follow.MagisterialDreadnought;
import org.example.constant.EffectTiming;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@EqualsAndHashCode(callSuper = true)
@Data
public class ForestGenesis extends SpellCard {
    public Integer cost = 1;
    public String name = "森林模式";
    public String job = "妖精";
    public String race = "灾厄";
    public String mark = """
        增加1张永恒树苗到牌堆中。
        """;

    public String subMark = "";

    @Override
    public void play(List<GameObj> targets) {
        super.play(targets);

        List<Card> addCards = new ArrayList<>();
        addCards.add(createCard(EternalSeedling.class));
        ownerPlayer().addDeck(addCards);
    }
}
