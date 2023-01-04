package org.example.card.stalker.spell.spell;

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
    public Map<SpellCard, Event.Play> clearEffects = new HashMap<>();

    public Preparation() {
        setPlay(new Play(
            () -> {
                ownerPlayer().getHand().forEach(card -> {
                    if(card instanceof SpellCard spellCard){
                        Integer cutCost = Math.min(spellCard.getCost(),2);
                        spellCard.setCost(spellCard.getCost() - cutCost);
                        cutCosts.put(spellCard,cutCost);

                        // 打出后伺机待发临时效果消失
                        Event.Play clearEffect = new Event.Play(() -> {
                            // 其他牌增费
                            cutCosts.forEach((spellCard1, cutCost1) ->
                                spellCard1.setCost(spellCard1.getCost() + cutCost1));
                            cutCosts.clear();

                            // 清除其他牌的临时效果
                            clearEffects.forEach((spellCard1, play) -> {
                                if(spellCard1.atHand()) spellCard1.getPlays().remove(play);
                            });
                        });
                        spellCard.getPlays().add(clearEffect);
                        clearEffects.put(spellCard,clearEffect);
                    }
                });
            }
        ));
    }
}
