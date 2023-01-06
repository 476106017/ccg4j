package org.example.card.stalker.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
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
                        Integer cutCost = Math.min(spellCard.getCost(),2);
                        spellCard.setCost(spellCard.getCost() - cutCost);
                        cutCosts.put(spellCard,cutCost);

                        // 打出后伺机待发临时效果消失
                        Play clearEffect = new Play(() -> {
                            // 其他牌增费
                            ownerPlayer().getHand().forEach(other -> {
                                if(other instanceof SpellCard otherSpell)
                                    otherSpell.getEffectsFrom(this).forEach(effect ->
                                        cutCosts.computeIfPresent(otherSpell,(k,v)->{
                                            k.setCost(k.getCost() + v);
                                            return 0;}));});});
                        spellCard.setPlay(clearEffect);
                    }
                });
            }));
    }
}
