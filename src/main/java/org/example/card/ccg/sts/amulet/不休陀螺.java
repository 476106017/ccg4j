package org.example.card.ccg.sts.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.POISON;

@Getter
@Setter
public class 不休陀螺 extends AmuletCard {
    public Integer cost = 1;
    public String name = "不休陀螺";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        出牌后：如果没有手牌，则抽一张牌
        """;

    public String subMark = "";

    public void init() {
        addEffects(new Effect(this,this, EffectTiming.AfterPlay,
            ()->{
                if(ownerPlayer().getHand().isEmpty()){
                    ownerPlayer().draw(1);
                }
            }));
    }
}
