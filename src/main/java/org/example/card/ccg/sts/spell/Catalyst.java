package org.example.card.ccg.sts.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.POISON;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Catalyst extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 2;
    public String name = "催化剂";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        将一名敌人的中毒层数翻倍（三倍）。
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            final Integer count = enemyPlayer().getCount(POISON);
            enemyPlayer().count(POISON,isUpgrade()?2*count:count);
        }));
    }

}
