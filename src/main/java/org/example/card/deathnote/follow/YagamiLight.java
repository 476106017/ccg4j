package org.example.card.deathnote.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class YagamiLight  extends FollowCard {
    private String name = "夜神月";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 1;
    private String job = "死亡笔记";
    private List<String> race = Lists.ofStr("人类");
    private String mark = """
        亡语：对敌方主战者造成与【夜神月在场时死亡笔记击杀数量】等量的伤害
        """;

    public String subMark = "夜神月在场时死亡笔记击杀数量为{damage}";

    public String getSubMark() {
        return subMark.replaceAll("\\{damage}",getCount()+"");
    }


    public YagamiLight() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            enemyPlayer().getLeader().damaged(this,getCount());
        })));
    }
}
