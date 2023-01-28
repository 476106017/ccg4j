package org.example.card.ccg.fairy.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class Fairy extends FollowCard {
    public Integer cost = 1;

    public String name = "妖精";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("妖精");
    public String mark = """
        瞬念召唤：回合结束时剩余1pp（不多不少）
        """;
    public String subMark = "";

    public int atk = 1;
    public int hp = 1;

    public Fairy() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.InvocationEnd,
            ()->ownerPlayer().getPpNum() == 1,
            ()->{})));
    }
}
