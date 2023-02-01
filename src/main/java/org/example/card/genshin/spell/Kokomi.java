package org.example.card.genshin.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.genshin.system.ElementCostSpellCard;
import org.example.card.genshin.system.Elemental;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class Kokomi extends ElementCostSpellCard {
    public List<Elemental> elementCost = List.of(Elemental.Hydro);
    public String name = "运筹帷幄";
    public String job = "原神";
    private List<String> race = Lists.ofStr();
    public String mark = """
    抽2张牌
    """;
    public String subMark = "";

    public Kokomi() {
        setPlay(new Play(()-> ownerPlayer().draw(2)));
    }
}