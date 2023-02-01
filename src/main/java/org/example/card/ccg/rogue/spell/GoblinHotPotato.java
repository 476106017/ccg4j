package org.example.card.ccg.rogue.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class GoblinHotPotato extends SpellCard {
    public Integer cost = 1;
    public String name = "地精烫手山芋";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        将此卡交给对手
        在手牌上回合结束时：对己方主战者造成5点伤害
        """;

    public String subMark = "";

    public GoblinHotPotato() {
        setPlay(new Play(()->{
            changeOwner();
            removeWhenNotAtArea();
            enemyPlayer().addHand(this);
        }));

        addEffects(new Effect(this,this, EffectTiming.EndTurnAtHand, obj->{
            info.damageEffect(this,ownerLeader(),5);
        }));

    }
}
