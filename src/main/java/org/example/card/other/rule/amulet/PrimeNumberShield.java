package org.example.card.other.rule.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.Lists;
import org.example.system.MyMath;

import java.util.List;

@Getter
@Setter
public class PrimeNumberShield extends AmuletCard {
    public Integer cost = 1;
    public String name = "质数盾";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr();
    public String mark = """
        只要这张卡片在场上，主战者只会受到的伤害只能是质数。
        (若伤害小于2，则受到2点伤害；若大于2且伤害值非质数，则受到与【小于伤害值的最大质数】等量的伤害)
        """;

    public String subMark = "";

    public PrimeNumberShield() {
        addEffects((new Effect(this,this, EffectTiming.WhenAtArea, ()->{
            ownerLeader().addEffect(new Effect(this, ownerLeader(), EffectTiming.BeforeDamaged,
                obj->{
                    Damage damage = (Damage) obj;

                    return damage.getDamage() > MyMath.maxPrimeNum(damage.getDamage());
                },
                obj->{
                    Damage damage = (Damage) obj;

                    int i = MyMath.maxPrimeNum(damage.getDamage());
                    int reduce = damage.getDamage() - i;

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
