package org.example.card.ccg.mage.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class DimensionShift extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 20;
    public String name = "次元超越";
    public String job = "法师";
    private List<String> race = Lists.ofStr();
    public String mark = """
        魔力增幅 消费-1
        获得额外回合
        """;

    public String subMark = "";


    public void init() {
        addEffects((new Effect(this,this,
            EffectTiming.Boost,obj->obj instanceof SpellCard, obj -> addCost(-1))));
        setPlay(new Play(()->{
            info.addMoreTurn();
        }));
    }

}
