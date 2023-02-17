package org.example.card.ccg.shaman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class SpiritCleanser extends FollowCard {
    private String name = "灵魂净化者";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 1;
    private String job = "萨满";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：将1个随从变身成其自身
        """;
    private String subMark = "";

    public SpiritCleanser() {
        setMaxHp(getHp());
        setPlay(new Play(
            ()->{
                List<GameObj> targets = enemyPlayer().getAreaFollowsAsGameObj();
                targets.addAll(ownerPlayer().getAreaFollowsAsGameObj());
                return targets;
            },
            false,
            targets->{
                FollowCard followCard = (FollowCard) targets;
                info.transform(followCard,followCard.createCard(followCard.getClass()));
            }));
    }
}