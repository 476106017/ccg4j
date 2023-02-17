package org.example.card.ccg.rogue.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class InfiltratorOnijus extends FollowCard {
    private String name = "渗透者奥妮朱思";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 2;
    private String job = "潜行者";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼:如果你的牌库、手牌和战场均有2个以上渗透者奥妮朱思，获得游戏胜利。
        """;
    private String subMark = "";

    public InfiltratorOnijus() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
                if(ownerPlayer().getDeckBy(card -> card instanceof InfiltratorOnijus).size()>1
                && ownerPlayer().getHandBy(card -> card instanceof InfiltratorOnijus).size()>1
                && ownerPlayer().getAreaBy(card -> card instanceof InfiltratorOnijus).size()>1){
                    info.gameset(ownerPlayer());
                }
            }));
    }
}
