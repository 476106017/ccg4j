package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class ltstt extends FollowCard {
    public Integer cost = 2;
    public String name = "立体声图腾";
    public String job = "中立";
    private List<String> race = Lists.ofStr("图腾");
    public String mark = """
        回合结束时：随机使手牌上的一张随从+2/+2
        """;
    public String subMark = "";

    public int atk = 0;
    public int hp = 3;

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->
        {
            FollowCard handRandomFollow = ownerPlayer().getHandRandomFollow();
            if(handRandomFollow!=null) handRandomFollow.addStatus(2,2);
        }
        )));
    }

}
