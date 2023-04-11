package org.example.card.ccg.druid.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class LightningBloom extends SpellCard {
    public Integer cost = 0;
    public String name = "雷霆绽放";
    public String job = "德鲁伊";
    private List<String> race = Lists.ofStr();
    public String mark = """
        使用下回合的两个法力水晶
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()->{
            ownerPlayer().addPp(2);
            ownerLeader().addEffect(new Effect(this,ownerLeader(),
                EffectTiming.BeginTurn,3,()-> ownerPlayer().addPp(-2)), false);
        }));
    }

}
