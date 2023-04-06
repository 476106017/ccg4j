package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class DarkfallenNeophyte extends FollowCard {
    private String name = "黑暗堕落者新兵";
    private Integer cost = 3;
    private int atk = 2;
    private int hp = 5;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        死灵术 2：使手牌中的随从获得+2/+0
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            ownerPlayer().costGraveyardCountTo(2,()->
                ownerPlayer().getHandFollows().forEach(followCard ->
                    followCard.addStatus(2,0)));
        }));
    }
}