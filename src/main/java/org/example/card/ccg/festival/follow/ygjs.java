package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class ygjs extends FollowCard {
    public Integer cost = 2;
    public String name = "摇滚巨石";
    public String job = "中立";
    private List<String> race = Lists.ofStr("元素");
    public String mark = """
        增幅：费用为1的牌，使本随从获得+1/+1
        """;
    public String subMark = "";

    public int atk = 2;
    public int hp = 2;

    private FollowCard targetFollow;

    public ygjs() {
        setMaxHp(getHp());
        getKeywords().add("突进");

        addEffects((new Effect(this,this, EffectTiming.Boost,
            card-> ((Card)card).getCost()==1,
            card-> addStatus(1,1)
        )));
    }

}
