package org.example.card.ccg.sts.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Barricade extends AmuletCard {
    public Integer cost = 4;
    public String name = "壁垒";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        战吼：格挡不再在你的回合开始时消失。(回馈1pp)
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(()-> {
            ownerPlayer().count("壁垒");
            if(isUpgrade())ownerPlayer().addPp(1);
        }));
    }
}
