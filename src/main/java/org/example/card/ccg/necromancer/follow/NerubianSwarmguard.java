package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NerubianSwarmguard extends FollowCard {
    private String name = "蛛魔护群守卫";
    private Integer cost = 4;
    private int atk = 1;
    private int hp = 3;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：召唤该随从的2个复制
        """;
    private String subMark = "";

    public NerubianSwarmguard() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            List<AreaCard> list = new ArrayList<>();
            list.add((AreaCard) clone());
            list.add((AreaCard) clone());
            ownerPlayer().summon(list);
        }));
    }
}