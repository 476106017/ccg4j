package org.example.card.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class ErosiveAnnihilation extends SpellCard {
    public Integer cost = 1;
    public String name = "绝命的腐蚀";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        回合结束时，敌方随从全体-1/-1，持续1回合
        腐蚀：持续时间延长1回合
        """;

    public String subMark = "当前持续{}回合";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    @Override
    public void initCounter() {
        this.count();
    }

    public ErosiveAnnihilation() {
        getPlays().add(new Event.Play(() ->
            // 创建主战者回合结束效果
            ownerPlayer().getLeader()
                .addEffect(this, EffectTiming.EndTurn, getCount(),false,damage ->{
                    List<FollowCard> enemyFollows =
                        enemyPlayer().getAreaFollowsAsFollow();
                    enemyFollows.forEach(followCard -> followCard.addStatus(-1,-1));
                })));

        getBoosts().add(new Event.Boost(
            card-> card.getCost()>=2,
            ()->count()
        ));
    }
}
