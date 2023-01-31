package org.example.card.ccg.neutral.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;
import java.util.Random;


@Getter
@Setter
public class RetracingThePast extends SpellCard {
    public Integer cost = 1;
    public String name = "记忆的轨迹";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        场上的2个友方随从获得【亡语：回复主战者1~2点生命】
        抽1张牌
        """;

    public String subMark = "";
    public int target = 1;

    @Override
    public void init() {
        this.count();
    }

    public RetracingThePast() {
        setPlay(new Play(()->
            List.of(ownerPlayer().getAreaFollowsAsGameObj(),ownerPlayer().getAreaFollowsAsGameObj()),
            2,true,
            obj->{
                obj.forEach(obj1 -> {
                    obj1.addEffects(new Effect(this,obj1, EffectTiming.DeathRattle,
                        ()-> obj1.ownerPlayer().heal(1+(int) (Math.random()*2))));
                });
                ownerPlayer().draw(1);
            }));
    }

}
