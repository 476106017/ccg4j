package org.example.card.ccg.neutral.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class TheCoin extends SpellCard {
    public Integer cost = 0;
    public String name = "幸运币";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        在本回合中，获得一个法力水晶。
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()->{
            ownerPlayer().addPp(1);
        }));
    }

}
