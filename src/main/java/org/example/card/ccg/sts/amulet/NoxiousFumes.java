package org.example.card.ccg.sts.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.POISON;
import static org.example.constant.CounterKey.STRENGTH;

@Getter
@Setter
public class NoxiousFumes extends AmuletCard {
    public Integer cost = 2;
    public String name = "毒雾";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        回合开始时：给予对手2（3）层中毒。
        """;

    public String subMark = "";

    public void init() {
        addEffects(new Effect(this,this, EffectTiming.BeginTurn,
            ()->{
                final int n = isUpgrade()?3:2;
                enemyPlayer().count(POISON,n);
            }));
    }
}
