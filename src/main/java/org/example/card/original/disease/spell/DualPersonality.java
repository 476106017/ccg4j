package org.example.card.original.disease.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Database;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class DualPersonality extends SpellCard {
    public Integer cost = 2;
    public String name = "双重人格";
    public String job = "疾病";
    private List<String> race = Lists.ofStr();
    public String mark = """
        指定一名友方随从，造成1点伤害以创造一个随机的幻象（幻象仅有1点生命值），
        当一名随从离场时，另一名将会消失
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObjBy(followCard -> followCard.getHp()>1),
            true,
            target->{
                if(target!=null){
                    FollowCard followCard = (FollowCard) target;
                    info.damageEffect(this,followCard,1);
                    final Card card = Database.getPrototypeBy(p -> p instanceof FollowCard);
                    final FollowCard followCard1 = (FollowCard) (card.copyBy(ownerPlayer()));
                    followCard1.setHp(1);
                    followCard1.setMaxHp(1);
                    followCard.addEffects((new Effect(this,followCard, EffectTiming.WhenNoLongerAtArea, ()->{
                        info.exile(followCard1);
                    })));
                    followCard1.addEffects((new Effect(this,followCard1, EffectTiming.WhenNoLongerAtArea, ()->{
                        info.exile(followCard);
                    })));
                }
            }));
    }

}
