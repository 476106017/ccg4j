package org.example.card.ccg.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.card.ccg.nemesis.Yuwan;
import org.example.game.Play;
import org.example.system.Database;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Syntonization extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 1;
    public String name = "创造物的同步";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("");
    public String mark = """
        ‧解析的创造物
        ‧增殖的创造物
        ‧绚烂的创造物
        ‧典范转移
        发现其中一张牌。
        """;

    public String subMark = "";
    public void init() {

        setPlay(new Play(
            ()-> ownerPlayer().discoverCard(
                List.of(Database.getPrototype(Yuwan.AnalyzingArtifact.class),
                    Database.getPrototype(Yuwan.BifurcatingArtifact.class),
                    Database.getPrototype(Yuwan.RadiantArtifact.class),
                    Database.getPrototype(Yuwan.ParadigmShift.class)),
            prototype -> ownerPlayer().addHand(prototype.copyBy(ownerPlayer())))));
    }

}
