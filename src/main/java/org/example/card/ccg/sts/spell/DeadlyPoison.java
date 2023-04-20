package org.example.card.ccg.sts.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.BLOCK;
import static org.example.constant.CounterKey.POISON;

@Getter
@Setter
public class DeadlyPoison extends SpellCard {
    public Integer cost = 2;
    public String name = "致命毒药";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        给与5（7）层中毒。
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            enemyPlayer().count(POISON,isUpgrade()?7:5);
        }));
    }

}
