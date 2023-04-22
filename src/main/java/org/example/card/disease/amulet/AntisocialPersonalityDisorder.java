package org.example.card.disease.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class AntisocialPersonalityDisorder extends AmuletCard {

    public Integer cost = 9;

    public String name = "反社会人格障碍";
    public String job = "疾病";
    private List<String> race = Lists.ofStr();

    public String mark = """
        场上随从具有【攻击时，受伤时：随机攻击一个目标】
        """;
    public String subMark = "";

    List<FollowCard> effectFollows = new ArrayList<>();

    public void init() {
        addEffects((new Effect(this,this,
            EffectTiming.WhenAtArea, obj-> info.getAreaFollowsCopy().forEach(this::sh))));
        addEffects((new Effect(this,this,
            EffectTiming.WhenSummon,areaCard -> sh(((List<AreaCard>)areaCard)))));
        addEffects((new Effect(this,this,
            EffectTiming.WhenEnemySummon,areaCard -> sh(((List<AreaCard>)areaCard)))));
        addEffects((new Effect(this,this,
            EffectTiming.WhenNoLongerAtArea, obj->
            effectFollows.forEach(((followCard) ->
                effectFollows.forEach(p -> p.getEffects().removeIf(effect -> effect.getParent().equals(this)))))
        )));
    }

    private void sh(List<AreaCard> areaCards){
        areaCards.forEach(this::sh);
    }
    private void sh(AreaCard areaCard){
        if(areaCard instanceof FollowCard followCard){
            effectFollows.add(followCard);

            followCard.addEffects(new Effect(this,followCard, EffectTiming.WhenAttack,obj->{
                Damage damage = (Damage) obj;
                final List<GameObj> list = info.getTargetableGameObj().stream().filter(p -> p != followCard).toList();
                damage.setTo(Lists.randOf(list));
            }));
            followCard.addEffects(new Effect(this,followCard, EffectTiming.AfterDamaged,obj->{
                final List<GameObj> list = info.getTargetableGameObj().stream().filter(p -> p != followCard).toList();
                followCard.attack(Lists.randOf(list));
            }));
        }
    }
}
