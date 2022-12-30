package org.example.card.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.card.fairy.follow.EternalSeedling;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
public class ForestGenesis extends SpellCard {
    public Integer cost = 1;
    public String name = "森林模式";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("灾厄");
    public String mark = """
        将1张永恒树苗洗入牌堆。
        """;

    public String subMark = "";

    public ForestGenesis() {
        getPlays().add(new Card.Event.Play(() -> {
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(EternalSeedling.class));
                ownerPlayer().addDeck(addCards);
            }
        ));
    }

}
