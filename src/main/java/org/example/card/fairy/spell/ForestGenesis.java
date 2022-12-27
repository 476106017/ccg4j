package org.example.card.fairy.spell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.card.fairy.follow.EternalSeedling;

import java.util.ArrayList;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class ForestGenesis extends SpellCard {
    public Integer cost = 1;
    public String name = "森林模式";
    public String job = "妖精";
    private List<String> race = List.of("灾厄");
    public String mark = """
        增加1张永恒树苗到牌堆中。
        """;

    public String subMark = "";

    public ForestGenesis() {
        getPlays().add(new Card.Event.Play(ArrayList::new,0,
            gameObjs -> {
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(EternalSeedling.class));
                ownerPlayer().addDeck(addCards);
            }
        ));
    }

}