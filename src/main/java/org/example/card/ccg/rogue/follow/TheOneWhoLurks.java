package org.example.card.ccg.rogue.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class TheOneWhoLurks extends FollowCard {
    private String name = "潜伏者";
    private Integer cost = 10;
    private int atk = 1;
    private int hp = 1;
    private String job = "潜行者";
    private List<String> race = Lists.ofStr();
    private String mark = """
        被抽到时：洗入对手牌库并失去该能力
        """;
    private String subMark = "";

    private boolean canEffect = true;

    public void init() {
        setMaxHp(getHp());

        addEffects((new Effect(this,this, EffectTiming.WhenDrawn,
            ()->canEffect,
            ()->{
                removeWhenNotAtArea();
                changeOwner();
                ownerPlayer().addDeck(this);
                setCanEffect(false);
        })));
    }
}
