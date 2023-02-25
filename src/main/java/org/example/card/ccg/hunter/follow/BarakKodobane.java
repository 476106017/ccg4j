package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class BarakKodobane extends FollowCard {
    private String name = "巴拉克·科多班恩";
    private Integer cost = 5;
    private int atk = 3;
    private int hp = 5;
    private String job = "猎人";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：搜索费用为1/2/3的法术牌各一张
        """;
    private String subMark = "";

    public BarakKodobane() {
        setMaxHp(getHp());
        setPlay(new Play(()-> {
            ownerPlayer().draw(card -> card instanceof SpellCard && card.getCost().equals(1));
            ownerPlayer().draw(card -> card instanceof SpellCard && card.getCost().equals(2));
            ownerPlayer().draw(card -> card instanceof SpellCard && card.getCost().equals(3));
        }));
    }

}