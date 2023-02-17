package org.example.card.lol.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class Garen extends FollowCard {
    private String name = "盖伦";
    private Integer cost = 5;
    private int atk = 3;
    private int hp = 9;
    private String job = "英雄联盟";
    private List<String> race = Lists.ofStr();
    private String mark = """
        攻击时：如果攻击随从，则造成与目标缺失生命值等量的伤害
        回合结束时：如果在本回合未受伤，则回复5点生命值
        """;
    private String subMark = "";

    private boolean turnDamaged = false;


    public Garen() {
        setMaxHp(getHp());
        addEffects(new Effect(this,this, EffectTiming.WhenAttack, obj->{
            Damage damage = (Damage) obj;
            if(damage.getTo() instanceof FollowCard toFollow){
                info.damageEffect(this,toFollow,toFollow.getMaxHp()-toFollow.getHp());
            }
        }));
        addEffects(new Effect(this, this, EffectTiming.AfterDamaged,o -> {
            setTurnDamaged(true);
        }));
        addEffects(new Effect(this, this, EffectTiming.EndTurn,() -> {
            if (!isTurnDamaged())
                this.heal(5);
            setTurnDamaged(false);
        }));
    }
}