package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Deathmolder extends FollowCard {
    private String name = "铸亡者";
    private Integer cost = 6;
    private int atk = 5;
    private int hp = 8;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        主战者受到疲劳伤害后，发动亡灵召还：X（X是本次伤害值）
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this,
            EffectTiming.AfterLeaderDamaged, obj -> {
            Damage damage = (Damage) obj;
            ownerPlayer().recall(damage.getDamage());
        })));
    }
}