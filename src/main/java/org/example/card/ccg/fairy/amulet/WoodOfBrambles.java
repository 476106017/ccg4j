package org.example.card.ccg.fairy.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class WoodOfBrambles extends AmuletCard {

    public Integer cost = 2;

    public String name = "荆棘之森";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("自然");

    public String mark = """
        战吼：增加2张妖精到手牌
        若此卡在场上，我方全部随从拥有【交战时：对交战对象造成1点伤害】效果
        """;
    public String subMark = "";

    public WoodOfBrambles() {
        setCountDown(2);
        setPlay(new Play(()->{
            ownerPlayer().addHand(createCard(Derivant.Fairy.class));
            ownerPlayer().addHand(createCard(Derivant.Fairy.class));
        }));
        addEffects((new Effect(this,this, EffectTiming.WhenAtArea, ignored->
            ownerPlayer().getAreaFollowsAsFollow().forEach(followCard -> {
                followCard.addEffects(new Effect(this,followCard,EffectTiming.WhenBattle,
                obj -> {
                    Damage damage = (Damage) obj;
                    FollowCard another = (FollowCard) damage.another(followCard);
                    info.damageEffect(followCard,another,1);
                    info.damageEffect(followCard,another,1);
                }));
            })
        )));
        addEffects((new Effect(this,this, EffectTiming.WhenSummon,areaCard -> {
            if(areaCard instanceof FollowCard followCard){
                followCard.addEffects(new Effect(this,followCard,EffectTiming.WhenBattle,
                obj -> {
                    Damage damage = (Damage) obj;
                    FollowCard another = (FollowCard) damage.another(followCard);
                    info.damageEffect(followCard,another,1);
                }));
            }
        })));
        addEffects((new Effect(this,this, EffectTiming.WhenNoLongerAtArea, obj->
            ownerPlayer().getAreaFollowsAsFollow().forEach(followCard ->
                followCard.getEffects().removeIf(effect -> effect.getParent().equals(this)))
        )));
    }

}
