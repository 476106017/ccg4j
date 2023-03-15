package org.example.card.ccg.neutral.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class AngelicSnipe extends SpellCard {
    public Integer cost = 1;
    public String name = "天使的圣箭";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        给予敌方的主战者或1个敌方的从者1点伤害。
        """;

    public String subMark = "";
    public int target = 1;

    @Override
    public void init() {
        this.count();
    }

    public AngelicSnipe() {
        setPlay(new Play(()->{
            List<GameObj> targetable = new ArrayList<>();
            targetable.add(info.oppositePlayer().getLeader());
            targetable.addAll(info.oppositePlayer().getAreaFollows());
            return targetable;
        },
            true,
            target->
                info.damageEffect(this,target,1)
            ));
    }

}
