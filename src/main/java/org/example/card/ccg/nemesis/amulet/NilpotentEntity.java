package org.example.card.ccg.nemesis.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;
import org.example.system.util.MyMath;

import java.util.List;

@Getter
@Setter
public class NilpotentEntity extends AmuletCard {
    public Integer cost = 1;
    public String name = "虚数物体";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        战吼：如果手牌有创造物卡片，则随机舍弃其中1张，并使此护符倒数变为4
        
        只要这个护符在战场上，自己的主战者所受到的伤害如果为4以上，则会转变为3。
        """;

    public String subMark = "";

    public void init() {
        setCountDown(2);
        setPlay(new Play(
            () -> {
                List<Card> artifact = ownerPlayer().getHandBy(card -> card.getRace().contains("创造物"));
                if(!artifact.isEmpty()){
                    ownerPlayer().abandon(Lists.randOf(artifact));
                    setCountDown(4);
                }
        }));

        addEffects((new Effect(this,this, EffectTiming.WhenAtArea, ()->{
            ownerLeader().addEffect(new Effect(this, ownerLeader(), EffectTiming.BeforeDamaged,
                obj->{
                    Damage damage = (Damage) obj;
                    return damage.getDamage() > 3;
                },
                obj->{
                    Damage damage = (Damage) obj;
                    damage.setDamage(3);
                    info.msg("虚数物体将本次伤害变成3");
                }),true);
        })));
        addEffects((new Effect(this,this, EffectTiming.WhenNoLongerAtArea, ()->{
            List<Effect> effectsFrom = ownerLeader().getEffectsFrom(this);
            ownerLeader().getEffects().removeAll(effectsFrom);
        })));
    }
}
