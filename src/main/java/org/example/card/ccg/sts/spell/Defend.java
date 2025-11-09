package org.example.card.ccg.sts.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.BLOCK;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Defend extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 2;
    public String name = "防御";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        获得5(8)点格挡。
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            ownerPlayer().count(BLOCK,isUpgrade()?8:5);
        }));
    }

}
