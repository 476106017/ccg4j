package org.example.morecard.xie.spell;

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
public class Spell01 extends SpellCard {
    public Integer cost = 0;
    public String name = "Spell01";
    public String job = "谢test";
    private List<String> race = Lists.ofStr();
    public String mark = """
        对一个随从造成2点伤害。
        """;

    public String subMark = "";
    public int target = 1;
    public void init() {
        setPlay(new Play(()->{
            List<GameObj> targetable = new ArrayList<>(info.oppositePlayer().getAreaFollows());
            return targetable;
        },
            true,
            target->
                info.damageEffect(this,target,2)
            ));
    }

}
