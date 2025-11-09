package org.example.card.ccg.rogue.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Hallucination extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 1;
    public String name = "幻觉";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        发现1张牌
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(()->ownerPlayer().discoverCard(card -> true,
            card ->  ownerPlayer().addHand(card.copyBy(ownerPlayer())))));
    }
}
