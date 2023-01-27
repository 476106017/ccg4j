package org.example.card.heartstone.stalker.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
public class Preparation extends SpellCard {
    public Integer cost = 0;
    public String name = "伺机待发";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        本回合的下一个法术费用-2
        """;

    public String subMark = "";

    public Map<SpellCard,Integer> cutCosts = new HashMap<>();

    public Preparation() {
        setPlay(new Play(
            () -> {
                ownerPlayer().getHand().forEach(card -> {
                    if(card instanceof SpellCard spellCard){
                        Integer cutCost = Math.min(spellCard.getCost(),1);
                        spellCard.setCost(spellCard.getCost() - cutCost);
                        cutCosts.put(spellCard,cutCost);

                        // 给主战者挂一个使用时
                        Leader leader = ownerPlayer().getLeader();
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
                        }), false);
                    }
                });
            }));
    }
}
