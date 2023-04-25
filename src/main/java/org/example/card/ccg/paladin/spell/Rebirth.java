package org.example.card.ccg.paladin.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Rebirth extends SpellCard {
    public Integer cost = 10;
    public String name = "重生";
    public String job = "圣骑士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        使你的主战者如获新生（净化所有效果并变为30生命值）
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()-> {
            ownerLeader().getEffects().clear();
            ownerPlayer().setHpMax(30);
            ownerPlayer().setHp(30);
        }));
    }

}
