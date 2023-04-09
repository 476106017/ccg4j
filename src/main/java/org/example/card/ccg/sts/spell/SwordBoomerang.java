package org.example.card.ccg.sts.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Damage;
import org.example.game.DamageMulti;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;
import org.example.system.util.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SwordBoomerang extends SpellCard {
    public Integer cost = 1;
    public String name = "飞剑回旋镖";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        随机对敌人造成3点伤害 3(4)次。
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            int n = isUpgrade()?4:3;

            Map<GameObj,Integer> damageMap = new HashMap<>();
            damageMap.put(enemyLeader(),0);
            enemyPlayer().getAreaFollowsAsGameObj()
                .forEach(gameObj -> damageMap.put(gameObj,0));
            for (int i = 0; i < n; i++) {
                GameObj obj = Maps.randomKey(damageMap);
                if(obj instanceof FollowCard followCard && followCard.getHp()<=damageMap.get(obj)){
                    i--;continue;
                }
                damageMap.merge(obj, 3, Integer::sum);
            }

            List<Damage> damages = new ArrayList<>();
            damageMap.forEach((gameObj, integer) -> {
                damages.add(new Damage(this,gameObj,integer));
            });
            new DamageMulti(info,damages).apply();
        }));
    }

}
