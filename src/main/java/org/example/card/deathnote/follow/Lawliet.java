package org.example.card.deathnote.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class Lawliet  extends FollowCard {
    private String name = "Lawliet";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "死亡笔记";
    private List<String> race = Lists.ofStr("人类");
    private String mark = """
        战吼：使用L作为名字
        """;
    private String subMark = "";


    public Lawliet() {
        setMaxHp(getHp());
        setPlay(new Play(()->setName("L")));
    }
}