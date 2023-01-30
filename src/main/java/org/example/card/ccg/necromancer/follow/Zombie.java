package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class Zombie extends FollowCard {
    private String name = "僵尸";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 2;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        """;
    private String subMark = "";

    public Zombie() {
        setMaxHp(getHp());
    }
}