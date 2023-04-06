package org.example.card.ccg.druid.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class LassyLoyalCompanion extends FollowCard {
    private String name = "忠犬拉叙";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "德鲁伊";
    private List<String> race = Lists.ofStr("野兽");
    private String mark = """
        瞬念召唤：回合开始时主战者不是满血状态，
        返回手牌并且主战者获得【受伤前：如果伤害值大于主战者生命值且手牌上有忠犬拉叙，则召唤其中1只到战场并承受本次伤害】
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
            ()->ownerPlayer().getHp() < ownerPlayer().getHpMax(),
            ()->{
                ownerLeader().addEffect(new Effect(this, ownerLeader(), EffectTiming.BeforeDamaged,
                    obj->{
                        Damage damage = (Damage) obj;
                        if(damage.getDamage()>ownerPlayer().getHp()) return true;
                        return false;
                    },
                    obj->{
                        Optional<Card> first = ownerPlayer().getHand().stream()
                            .filter(card -> card instanceof LassyLoyalCompanion).findFirst();
                        first.ifPresent(card -> {
                            info.msg("忠犬拉叙跳下战场为"+ownerPlayer().getName()+"承受了本次伤害！");
                            LassyLoyalCompanion lassyLoyalCompanion = (LassyLoyalCompanion) card;
                            Damage damage = (Damage) obj;

                            ownerPlayer().summon(lassyLoyalCompanion);
                            damage.setTo(lassyLoyalCompanion);
                    });
                }),true);
            }
        )));
    }
}