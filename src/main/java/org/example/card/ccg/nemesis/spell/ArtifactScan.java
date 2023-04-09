package org.example.card.ccg.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Getter
@Setter
public class ArtifactScan extends SpellCard {
    public Integer cost = 1;
    public String name = "创造物扫描";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("");
    public String mark = """
        从墓地搜索2张创造物卡置入手牌
        如果搜索前墓地中创造物卡名称为6种以上，则使这两张卡费用变为0
        """;

    public String subMark = "";
    public void init() {

        setPlay(new Play(
            ()->{
                final List<Card> artifacts = ownerPlayer().getGraveyardBy(p -> p.hasRace("创造物"));
                final int size = artifacts.stream().map(Card::getName).distinct().toList().size();
                final List<Card> rand2 = Lists.randOf(artifacts, 2);
                if(size>=6)
                    rand2.forEach(card -> card.setCost(0));// 使这两张卡费用变为0
                rand2.forEach(Card::removeWhenNotAtArea);
                ownerPlayer().addHand(rand2);
            }));
    }

}
