package org.example.card.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.GameObj;
import org.example.system.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;


@Getter
@Setter
public class HeroicResolve extends SpellCard {
    public Integer cost = 1;
    public String name = "英雄的觉悟";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        回合结束时，抽X张卡牌，己方随从全体+X/+X。（X是本回合使用的卡牌数/4）
        """;

    public String subMark = "X等于{}";
    public String getSubMark() {
        return subMark.replaceAll("\\{}",ownerPlayer().getCount(PLAY_NUM)/ 4+"");
    }


    public HeroicResolve() {
        getPlays().add(new Event.Play(()->
            // 创建主战者回合结束效果
            ownerPlayer().getLeader()
                .addEffect(this, EffectTiming.EndTurn, 1,false, damage ->{
                    int x = ownerPlayer().getCount(PLAY_NUM) / 4;

                    ownerPlayer().draw(x);
                    ownerPlayer().getAreaFollowsAsFollow().forEach(followCard -> followCard.addStatus(x,x));
                })));
    }
}
