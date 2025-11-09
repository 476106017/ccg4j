package org.example.card.ccg.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.card._derivant.Derivant;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class FairyCircle extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 1;
    public String name = "妖精的呼朋引伴";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        增加2张妖精卡片到手牌中。
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(() -> {
            List<Card> addCards = new ArrayList<>();
            addCards.add(createCard(Derivant.Fairy.class));
            addCards.add(createCard(Derivant.Fairy.class));
            ownerPlayer().addHand(addCards);
        }));
    }
}
