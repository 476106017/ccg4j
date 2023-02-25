package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.*;
import org.example.system.util.Lists;
import org.example.system.util.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
public class SireDenathrius extends FollowCard {
    private String name = "德纳修斯大帝";
    private Integer cost = 10;
    private int atk = 10;
    private int hp = 10;
    private String job = "中立";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：对所有敌人造成总计5点伤害。无限注能（1）：伤害+1。
        """;
    public String subMark = "注能次数：{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    public SireDenathrius() {
        setMaxHp(getHp());
        getKeywords().add("吸血");
        addEffects((new Effect(this,this,
            EffectTiming.Charge, obj -> count())));
        setPlay(new Play(()->{
            int n = 5 + getCount();

            Map<GameObj,Integer> damageMap = new HashMap<>();
            damageMap.put(enemyLeader(),0);
            enemyPlayer().getAreaFollowsAsGameObj()
                .forEach(gameObj -> damageMap.put(gameObj,0));
            for (int i = 0; i < n; i++) {
                GameObj obj = Maps.randomKey(damageMap);
                if(obj instanceof FollowCard followCard && followCard.getHp()<=damageMap.get(obj)){
                    i--;continue;
                }
                damageMap.merge(obj, 1, Integer::sum);
            }

            List<Damage> damages = new ArrayList<>();
            damageMap.forEach((gameObj, integer) -> {
                damages.add(new Damage(this,gameObj,integer));
            });
            new DamageMulti(info,damages).apply();
        }));
    }
}