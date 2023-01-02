package org.example.card.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.system.Lists;

import java.util.Comparator;
import java.util.List;


@Getter
@Setter
public class Chronos extends FollowCard {
    private String name = "克罗诺斯";
    private Integer cost = 8;
    private int atk = 4;
    private int hp = 3;
    private String job = "中立";
    private List<String> race = Lists.ofStr("神");
    private String mark = """
        战吼：双方主战者获得唯一效果【回合结束时，抽X张牌（X是自己战场随从的最高攻击力）】
        """;
    private String subMark = "";

    public Chronos() {
        setMaxHp(getHp());
        getKeywords().add("突进");
        getPlays().add(new Card.Event.Play(()->{
            ownerPlayer().getLeader().addEffect(this, EffectTiming.EndTurn, ()->
                ownerPlayer().getAreaFollowsAsFollow().stream()
                    .max(Comparator.comparing(FollowCard::getAtk))
                    .ifPresent(followCard -> ownerPlayer().draw(followCard.getAtk())));
            enemyPlayer().getLeader().addEffect(this, EffectTiming.EndTurn, ()->
                enemyPlayer().getAreaFollowsAsFollow().stream()
                    .max(Comparator.comparing(FollowCard::getAtk))
                    .ifPresent(followCard -> enemyPlayer().draw(followCard.getAtk())));
        }));
    }
}