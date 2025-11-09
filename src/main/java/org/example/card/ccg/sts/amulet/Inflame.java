package org.example.card.ccg.sts.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.STRENGTH;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Inflame extends AmuletCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 2;
    public String name = "燃烧";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        战吼：获得2(3)点力量
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(()-> {
            ownerPlayer().count(STRENGTH,isUpgrade()?3:2);
        }));
    }
}
