package org.example.card.ccg.warlock.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class SoulEater extends FollowCard {
    private String name = "吞灵者";
    private Integer cost = 5;
    private int atk = 5;
    private int hp = 5;
    private String job = "术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        玩家在召唤时受到疲劳伤害
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this,
            EffectTiming.WhenSummon, areaCard -> ownerPlayer().wearyDamaged())));
        addEffects((new Effect(this,this,
            EffectTiming.WhenEnemySummon,areaCard -> enemyPlayer().wearyDamaged())));

    }
}