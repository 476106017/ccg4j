package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class IrondeepTrogg extends FollowCard {
    private String name = "深铁穴居人";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 2;
    private String job = "中立";
    private List<String> race = Lists.ofStr();
    private String mark = """
        敌方出牌时：如果打出的法术牌，则召唤另一个深铁穴居人
        """;
    private String subMark = "";

    public IrondeepTrogg() {
        setMaxHp(getHp());
        addEffects(new Effect(this,this, EffectTiming.WhenEnemyPlay,
            card->card instanceof SpellCard,
            card->ownerPlayer().summon(createCard(IrondeepTrogg.class))));
    }
}