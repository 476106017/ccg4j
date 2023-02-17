package org.example.card.paripi.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class CallingOtaku extends FollowCard {
    private String name = "打Call宅男";
    private Integer cost = 1;
    private int atk = 0;
    private int hp = 1;
    private String job = "派对咖";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：使1名随机友方随从+1/+0
        亡语：回到手牌
        """;
    private String subMark = "";

    public CallingOtaku() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            FollowCard followCard = (FollowCard)ownerPlayer().getAreaRandomFollow();
            followCard.addStatus(1,0);
        }));
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            removeWhenNotAtArea();
            ownerPlayer().addHand(this);
        })));
    }
}