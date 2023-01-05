package org.example.card.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
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
        setPlay(new Play(()->
            // 创建主战者回合结束效果
            ownerPlayer().getLeader().addEffect(new Effect(
                this,ownerPlayer().getLeader(),EffectTiming.EndTurn,
                getCount(),() ->{
                    List<FollowCard> enemyFollows =
                        enemyPlayer().getAreaFollowsAsFollow();
                    enemyFollows.forEach(followCard -> followCard.addStatus(-1,-1));
                }),false)
        ));

        addEffects((new Effect(this,this, EffectTiming.Boost,
            card-> ((Card)card).getCost()>=2,
            card->count()
        )));
    }
}
