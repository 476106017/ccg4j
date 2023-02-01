package org.example.card.ccg.rogue.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class Zuljin extends FollowCard {
    private String name = "祖尔金";
    private Integer cost = 6;
    private int atk = 5;
    private int hp = 5;
    private String job = "潜行者";
    private List<String> race = Lists.ofStr();
    private String mark = """
        亡语：如果你的PP>0，则消耗全部PP召还此随从
        """;
    private String subMark = "";

    public Zuljin() {
        setMaxHp(getHp());

        addEffects((new Effect(this,this, EffectTiming.DeathRattle,
            ()->ownerPlayer().getPpNum()>0,
            ()->{
                ownerPlayer().setPpNum(0);
                ownerPlayer().recall(this);
        })));
    }
}
