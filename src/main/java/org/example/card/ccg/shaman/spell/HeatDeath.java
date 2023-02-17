package org.example.card.ccg.shaman.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class HeatDeath extends SpellCard {
    public Integer cost = 2;
    public String name = "热寂";
    public String job = "萨满";
    private List<String> race = Lists.ofStr();
    public String mark = """
        抽2张牌
        将4张热寂洗入牌堆
        """;

    public String subMark = "";


    public HeatDeath() {
        setPlay(new Play(()->{
                ownerPlayer().draw(2);
                ownerPlayer().addDeck(createCard(HeatDeath.class));
                ownerPlayer().addDeck(createCard(HeatDeath.class));
                ownerPlayer().addDeck(createCard(HeatDeath.class));
                ownerPlayer().addDeck(createCard(HeatDeath.class));
            }
        ));
    }

}
