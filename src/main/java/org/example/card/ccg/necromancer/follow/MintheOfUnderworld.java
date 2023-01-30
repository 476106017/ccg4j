package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class MintheOfUnderworld extends FollowCard {
    private String name = "灵魂守门人·命忒";
    private Integer cost = 3;
    private int atk = 2;
    private int hp = 2;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        入场时：墓地+20
        离场时：墓地-20
        发动死灵术时：回复死灵术所消耗的墓地数
        """;
    private String subMark = "";

    public MintheOfUnderworld() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this,
            EffectTiming.Entering, obj -> {
            ownerPlayer().countToGraveyard(20);
        })));
        addEffects((new Effect(this,this,
            EffectTiming.Leaving, obj -> {
            ownerPlayer().countToGraveyard(-20);
        })));
        addEffects((new Effect(this,this,
            EffectTiming.WhenCostGraveyard, obj -> {
            ownerPlayer().countToGraveyard((Integer)obj);
        })));
    }
}