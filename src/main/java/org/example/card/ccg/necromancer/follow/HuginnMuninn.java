package org.example.card.ccg.necromancer.follow;

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
public class HuginnMuninn extends FollowCard {
    private String name = "福金与雾尼";
    private Integer cost = 3;
    private int atk = 1;
    private int hp = 1;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：召唤1个福金与雾尼，
        如果本场对战中发动亡语的卡牌数大于5，场上全部的福金与雾尼获得+1/+0、突进
        亡语：抽1张牌
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(
            () -> {
                ownerPlayer().summon(createCard(HuginnMuninn.class));
                if(ownerPlayer().getCount(EffectTiming.DeathRattle.getName()) >= 5){
                    ownerPlayer().getAreaFollowsAsFollowBy(followCard -> followCard instanceof HuginnMuninn)
                        .forEach(followCard ->{
                            followCard.addStatus(1,0);
                            followCard.addKeyword("突进");
                        });
                }
            }));
        addEffects((new Effect(this,this,
            EffectTiming.DeathRattle, obj -> {
            ownerPlayer().draw(1);
        })));
    }
}