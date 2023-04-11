package org.example.card.ccg.mage.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class FatesHand extends SpellCard {
    public Integer cost = 5;
    public String name = "命运的指引";
    public String job = "法师";
    private List<String> race = Lists.ofStr();
    public String mark = """
        魔力增幅 消费-1
        抽2张牌
        """;

    public String subMark = "";


    public void init() {
        addEffects((new Effect(this,this,
            EffectTiming.Boost,obj->obj instanceof SpellCard, obj -> addCost(-1))));
        setPlay(new Play(()->{
            ownerPlayer().draw(2);
        }));
    }

}
