package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class PrinceRenathal extends FollowCard {
    private String name = "雷纳索尔王子";
    private Integer cost = 3;
    private int atk = 3;
    private int hp = 4;
    private String job = "中立";
    private List<String> race = Lists.ofStr();
    private String mark = """
        瞬念召唤：对战开始时，除外该随从，使hp最大值变成40
        """;
    private String subMark = "";

    public PrinceRenathal() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
            ()->true,
            ()->{
                info.exile(this);
                info.msg(getNameWithOwner()+"使主战者的hp上限变成40");
                ownerPlayer().setHp(40);
                ownerPlayer().setHpMax(40);
            })));
    }
}