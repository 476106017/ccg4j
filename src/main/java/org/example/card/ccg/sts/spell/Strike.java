package org.example.card.ccg.sts.spell;

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
public class Strike extends SpellCard {
    public Integer cost = 2;
    public String name = "打击";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        造成6(9)点伤害
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            List<GameObj> targetable = new ArrayList<>();
            targetable.add(info.oppositePlayer().getLeader());
            targetable.addAll(info.oppositePlayer().getAreaFollows());
            return targetable;
        },
            true,
            target->
                info.damageEffect(this,target,isUpgrade()?9:6)
        ));
    }

}
