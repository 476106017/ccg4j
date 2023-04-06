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
public class PrimeNumberShield extends AmuletCard {
    public Integer cost = 1;
    public String name = "质数盾";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        战吼：如果手牌有创造物卡片，则随机舍弃其中1张，进化此卡
        
        只要这张卡片在场上，主战者受到的伤害只能是质数：
        若伤害小于2，则受到2点伤害；
        若伤害大于2且伤害值非质数，则受到与小于伤害值的最大质数等量的伤害
        如果已进化，去除【且伤害值非质数】的限制
        """;

    public String subMark = "";

    public void init() {
        setCountDown(3);
        setPlay(new Play(
            () -> {
                List<Card> artifact = ownerPlayer().getHandBy(card -> card.getRace().contains("创造物"));
                if(!artifact.isEmpty()){
                    ownerPlayer().abandon(Lists.randOf(artifact));
                    upgrade();
                }
        }));

        addEffects((new Effect(this,this, EffectTiming.WhenAtArea, ()->{
            ownerLeader().addEffect(new Effect(this, ownerLeader(), EffectTiming.BeforeDamaged,
                obj->{
                    Damage damage = (Damage) obj;

                    int damagei = damage.getDamage();
                    return damagei<2 || (damagei>2 && isUpgrade()) || damagei > MyMath.maxPrimeNum(damagei);
                },
                obj->{
                    Damage damage = (Damage) obj;

                    int damagei = damage.getDamage();
                    if(damagei<=2){
                        damage.setDamage(2);
                        info.msg("质数盾将本次伤害变成2");
                        return;
                    }

                    int i = MyMath.maxPrimeNum(isUpgrade()?damagei-1:damagei);
                    int reduce = damagei - i;

                    info.msg("质数盾为主战者抵挡了"+reduce+"点伤害");
                    damage.setDamage(i);
                }),true);
        })));
        addEffects((new Effect(this,this, EffectTiming.WhenNoLongerAtArea, ()->{
            List<Effect> effectsFrom = ownerLeader().getEffectsFrom(this);
            ownerLeader().getEffects().removeAll(effectsFrom);
        })));
    }
}
