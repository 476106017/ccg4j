package org.example.card.paripi.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.paripi.Kongming;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class DragonTailMan extends FollowCard {
    private String name = "难以接近的龙尾人";
    private Integer cost = 2;
    private int atk = 3;
    private int hp = 3;
    private String job = "派对咖";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：破坏自己全部随从，派对热度+X（X是破坏随从的数量）
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            int destroy = destroy(ownerPlayer().getAreaFollows());
            if(ownerLeader() instanceof Kongming kongming){
                kongming.addPartyHot(destroy);
            }


        }));

    }
}