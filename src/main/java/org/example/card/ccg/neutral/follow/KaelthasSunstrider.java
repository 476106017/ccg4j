package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.system.util.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.constant.CardRarity;


@Getter
@Setter
public class KaelthasSunstrider extends FollowCard {

   private CardRarity rarity = CardRarity.RAINBOW;
    public Integer cost = 6;
    public String name = "凯尔萨斯·逐日者";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        在每回合中，你每施放3个法术，第3个法术的法力值消耗为0点。
        """;
    public String subMark = "";


    public int atk = 4;
    public int hp = 7;

    public Map<SpellCard,Integer> cutCosts = new HashMap<>();
    public void init() {
        setMaxHp(getHp());
        // 打出时，已打出1张就可以调整费用了
        addEffects((new Effect(this,this, EffectTiming.WhenPlay,
            obj-> obj instanceof SpellCard, obj->ability(1))));

        // 入场时，已打出2张就调整费用
        addEffects((new Effect(this,this, EffectTiming.Entering, n -> ability(2))));
    }

    private void ability(int n){

        final long count = ownerPlayer().getPlayedCard()
            .stream().filter(p -> p instanceof SpellCard).count();
        if(count%3==n){
            // region 费用减少
            ownerPlayer().getHand().forEach(card -> {
                if(card instanceof SpellCard spellCard){
                    Integer cutCost = spellCard.getCost();
                    spellCard.setCost(0);
                    cutCosts.put(spellCard,cutCost);

                    // 给主战者挂一个使用时
                    Leader leader = ownerLeader();
                    leader.addEffect(new Effect(this,leader, EffectTiming.WhenPlay,1, o ->{
                        // 如果打的牌在影响列表，则影响列表的其他牌费用加回来
                        if(cutCosts.containsKey(o)){
                            cutCosts.forEach((k, v)->{
                                if(k!=o){
                                    k.setCost(k.getCost() + v);
                                }
                            });
                            // 移除这个使用时
                            leader.getEffects().removeAll(leader.getEffectsFrom(this));
                        }
                    }), true);
                }
            });
            // endregion 费用减少
            ownerLeader().addEffect(new Effect(this,ownerLeader(), EffectTiming.EndTurn,1,
                o ->
                    // 影响列表牌费用加回来
                    cutCosts.forEach((k, v)->{
                        if(k!=o){
                            k.addCost(v);
                        }
                    })
            ), true);
        }
    }
}
