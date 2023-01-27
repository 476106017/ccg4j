package org.example.card.shadowverse.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.Comparator;
import java.util.List;


@Getter
@Setter
public class Hamsa extends FollowCard {
    private String name = "汉萨";
    private Integer cost = 2;
    private int atk = 0;
    private int hp = 2;
    private String job = "中立";
    private List<String> race = Lists.ofStr("野兽");
    private String mark = """
        战吼：+X/+0（X是敌方战场随从的最高攻击力）
        """;
    private String subMark = "";

    public Hamsa() {
        setMaxHp(getHp());
        setPlay(new Play(()->
            enemyPlayer().getAreaFollowsAsFollow().stream()
                .max(Comparator.comparing(FollowCard::getAtk))
                .ifPresent(followCard -> addStatus(followCard.getAtk(),0))
        ));

    }
}