package org.example.card.ccg.warlock.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class DimensiusTheAllDevouring extends FollowCard {
    private String name = "诸界吞噬者迪蒙修斯";
    private Integer cost = 10;
    private int atk = 10;
    private int hp = 10;
    private String job = "术士";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        瞬念召唤：对战开始时，除外该随从，并在超抽时重新召唤
        """;
    private String subMark = "";

    public DimensiusTheAllDevouring() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
            ()->true,
            ()->{
                info.exile(this);
                Leader leader = ownerLeader();
                leader.addEffect(new Effect(this,leader, EffectTiming.WhenOverDraw,damage->{
                    ownerPlayer().summon(this);
                }), true);
            })));
    }
}