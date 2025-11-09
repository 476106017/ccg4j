package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Thoth extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "托特";
    private Integer cost = 2;
    private int atk = 1;
    private int hp = 2;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：如果本场对战中发动亡语的卡牌数大于10，主战者获得唯一效果【召唤时：被召唤的随从获得【亡语：对敌方主战者造成2点伤害】】
        亡语：抽1张牌
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            ownerPlayer().draw(1);
        })));
        setPlay(new Play(()->{
            if(ownerPlayer().getCount(EffectTiming.DeathRattle.getName()) >= 10){
                // 召唤时：被召唤的随从获得【亡语：对敌方主战者造成2点伤害】
                ownerLeader().addEffect(new Effect(this,ownerLeader(), EffectTiming.WhenSummon,obj->{
                    List<AreaCard> summonedCards = (List<AreaCard>) obj;
                    summonedCards.forEach(areaCard ->
                        // 【亡语：对敌方主战者造成2点伤害】
                        areaCard.addEffects(new Effect(this,areaCard, EffectTiming.DeathRattle,()->
                            info.damageEffect(areaCard,areaCard.enemyLeader(),2))));
                }), true);
            }
        }));
    }
}
