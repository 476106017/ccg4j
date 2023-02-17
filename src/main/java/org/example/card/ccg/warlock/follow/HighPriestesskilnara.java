package org.example.card.ccg.warlock.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class HighPriestesskilnara extends FollowCard {
    private String name = "高阶祭司基尔娜拉";
    private Integer cost = 5;
    private int atk = 4;
    private int hp = 5;
    private String job = "术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：指定1名随从，为你的英雄恢复等同于其攻击力的生命值，并使其攻击力变为0
        """;
    private String subMark = "";

    public HighPriestesskilnara() {
        setMaxHp(getHp());
        setPlay(new Play(()->info.getAreaFollowsAsGameObj(),false,
            obj->{
                if(obj instanceof FollowCard followCard){
                    ownerPlayer().heal(followCard.getAtk());
                    followCard.setAtk(0);
                }
        }));
    }
}