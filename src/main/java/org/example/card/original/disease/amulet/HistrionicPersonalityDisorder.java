package org.example.card.original.disease.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class HistrionicPersonalityDisorder extends AmuletCard {


   private CardRarity rarity = CardRarity.GOLD;
    public Integer cost = 2;

    public String name = "戏剧化人格障碍";
    public String job = "疾病";
    private List<String> race = Lists.ofStr();

    public String mark = """
        使用法术时，我方场上所有随从获得+1/-1
        使用随从时，我方场上所有随从获得-1/+1
        亡语：破坏我方场上所有随从
        """;
    public String subMark = "";

    public void init() {
        setCountDown(3);
        addEffects((new Effect(this,this, EffectTiming.WhenPlay,
            card-> card instanceof SpellCard || card instanceof FollowCard,
            card->{
                if(card instanceof SpellCard){
                    ownerPlayer().getAreaFollowsAsFollow().forEach(followCard -> followCard.addStatus(1,-1));
                }
                if(card instanceof FollowCard){
                    ownerPlayer().getAreaFollowsAsFollow().forEach(followCard -> followCard.addStatus(-1,1));
                }
            })));
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            destroy(ownerPlayer().getAreaFollows());
        })));
    }

}
