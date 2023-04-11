package org.example.card.ccg.druid.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class AquaticForm extends SpellCard {
    public Integer cost = 0;
    public String name = "水栖形态";
    public String job = "德鲁伊";
    private List<String> race = Lists.ofStr();
    public String mark = """
        探底。如果你在本回合中有足够的法力值使用选中的牌，则抽取这张牌。
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()->{
            final List<Card> deck = ownerPlayer().getDeck();
            final List<Card> tail = deck.subList(Math.max(deck.size() - 3, 0), deck.size());
            ownerPlayer().discoverCard(tail,discoverCard ->{
                discoverCard.removeWhenNotAtArea();
                if(discoverCard.getCost()>ownerPlayer().getPpNum()){
                    ownerPlayer().getDeck().add(0,discoverCard);
                }else {
                    ownerPlayer().addHand(discoverCard);
                }
            });
        }));
    }

}
