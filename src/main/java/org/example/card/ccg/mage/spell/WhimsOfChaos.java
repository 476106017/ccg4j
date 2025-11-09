package org.example.card.ccg.mage.spell;

import ch.qos.logback.core.testUtil.RandomUtil;
import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Func;
import org.example.system.util.Lists;

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;
import org.example.constant.CardRarity;

@Getter
@Setter
public class WhimsOfChaos extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 5;
    public String name = "混沌的行仪";
    public String job = "法师";
    private List<String> race = Lists.ofStr();
    public String mark = """
        在双方回合开始时，打乱手牌费用
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()-> ownerLeader().addEffect(new Effect(this, ownerLeader(), EffectTiming.BeginTurn,() -> {
            ownerPlayer().getHand().forEach(card -> {
                final int ppMax = ownerPlayer().getPpMax();
                final int cost = RandomGenerator.getDefault().nextInt(0,ppMax);
                card.setCost(cost);
            });
        }),true)));
    }

}
