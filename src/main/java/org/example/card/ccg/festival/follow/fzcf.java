package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class fzcf extends FollowCard {
    public Integer cost = 3;
    public String name = "服装裁缝";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        战吼：使场上随机一个友方随从获得本随从的数值
        """;
    public String subMark = "";

    public int atk = 2;
    public int hp = 2;

    public fzcf() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            FollowCard follow = (FollowCard) ownerPlayer().getAreaRandomFollow();
            follow.addStatus(getAtk(),getHp());
        }));
    }

}
