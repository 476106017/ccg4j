package org.example.card.ccg.hunter.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Getter
@Setter
public class Tracking extends SpellCard {
    public Integer cost = 1;
    public String name = "追踪术";
    public String job = "猎人";
    private List<String> race = Lists.ofStr();
    public String mark = """
        从牌堆发现1张牌
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(()->{
            Map<String, List<Card>> index = ownerPlayer().getDeckCopy()
                .stream().collect(Collectors.groupingBy(Card::getName));
            List<Card> cards = ownerPlayer().getDeckCopy().stream()
                .map(Card::getName)
                .distinct()
                .map(name->index.get(name).get(0)).toList();


            ownerPlayer().discoverCard(cards,discoverCard-> {
                discoverCard.removeWhenNotAtArea();
                ownerPlayer().addHand(discoverCard);
            });
        }));
    }
}
