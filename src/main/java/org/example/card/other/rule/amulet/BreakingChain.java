package org.example.card.other.rule.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class BreakingChain extends AmuletCard {
    public Integer cost = 4;
    public String name = "连锁终结";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr();
    public String mark = """
        若此卡在场上，本场游戏无法连锁
        """;

    public String subMark = "连锁：由于触发能力而导致触发其他的能力";

    public void init() {
        addEffects((new Effect(this,this, EffectTiming.WhenAtArea, obj->{
            info.setCanChain(false);
        })));
        addEffects((new Effect(this,this, EffectTiming.WhenNoLongerAtArea, obj->{
            info.setCanChain(true);
        })));
    }
}
