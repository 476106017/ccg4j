package org.example.card.ccg.warlock.spell;

import lombok.Getter;
import lombok.Setter;
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
public class Soulfire extends SpellCard {
    public Integer cost = 1;
    public String name = "灵魂之火";
    public String job = "术士";
    private List<String> race = Lists.ofStr("火焰");
    public String mark = """
        造成4点伤害，随机弃1张牌
        """;

    public String subMark = "";


    public Soulfire() {
        setPlay(new Play(()->{
            List<GameObj> targetable = new ArrayList<>();
            targetable.add(info.oppositePlayer().getLeader());
            targetable.addAll(info.oppositePlayer().getAreaFollows());
            return targetable;
        },
            true,
            target->{
                if(target instanceof FollowCard followCard){
                    info.damageEffect(this,followCard,4);
                } else if (target instanceof Leader leader) {
                    leader.damaged(this,4);
                }
                ownerPlayer().abandon(1);
            }));
    }

}
