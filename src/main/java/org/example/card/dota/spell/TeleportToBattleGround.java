package org.example.card.dota.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.constant.CardRarity;


@Getter
@Setter
public class TeleportToBattleGround extends SpellCard {

   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 0;
    public String name = "TP进场";
    public String job = "dota";
    private List<String> race = Lists.ofStr();
    public String mark = """
        本回合下一个英雄费用-1，并在使用时获得【突进】
        如果使用时场上有防御塔，则改为获得【疾驰】
        """;

    public String subMark = "";

    public Map<FollowCard,Integer> cutCosts = new HashMap<>();

    public void init() {
        setPlay(new Play(
            () -> {
                ownerPlayer().getHand().forEach(card -> {
                    if(card instanceof FollowCard followCard && followCard.hasRace("英雄")){
                        Integer cutCost = Math.min(followCard.getCost(),1);
                        followCard.setCost(followCard.getCost() - cutCost);
                        followCard.getKeywords().add("突进");
                        cutCosts.put(followCard,cutCost);
                    }
                });

                // 给主战者挂一个使用时
                Leader leader = ownerLeader();
                leader.addEffect(new Effect(this,leader,EffectTiming.WhenPlay,1,o ->{
                    // 如果打的牌在影响列表，则影响列表的其他牌费用加回来
                    if(cutCosts.containsKey(o)){
                        cutCosts.forEach((k, v)->{
                            if(k!=o){
                                k.setCost(k.getCost() + v);
                                k.removeKeyword("突进");
                            }
                        });
                        // 移除这个使用时
                        leader.getEffects().removeAll(leader.getEffectsFrom(this));
                    }
                }), false);
            }));
    }
}
