package org.example.card.ccg.fairy.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class SpringGreenProtection extends AmuletCard {

    public Integer cost = 1;

    public String name = "新绿的加护";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();

    public String mark = """
        回合结束时：随机给予1个自己的从者+1/+0效果。
        离场时：抽1张牌
        """;
    public String subMark = "";

    public SpringGreenProtection() {
        setCountDown(3);
        addEffects((new Effect(this,this, EffectTiming.EndTurn, obj-> {
            final FollowCard follow = (FollowCard) ownerPlayer().getAreaRandomFollow();
            if(follow!=null)follow.addStatus(1,0);
        }
        )));

        addEffects((new Effect(this,this, EffectTiming.Leaving, obj->
            ownerPlayer().draw(1)
        )));
    }

}
