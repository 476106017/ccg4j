package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class TheGalaxyGuide extends FollowCard {
    private String name = "银河系向导";
    private Integer cost = 3;
    private int atk = 4;
    private int hp = 2;
    private String job = "中立";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：如果在卡牌上的总消耗pp恰好是42，使对手pp最大值归零；
        如果打出卡牌数恰好是42，除外对方战场及牌库所有牌
        """;
    private String subMark = "";

    public TheGalaxyGuide() {
        setMaxHp(getHp());
        setPlay(new Play(()->// TODO 施工中
            ownerPlayer().draw(card -> card.getCost().equals(ownerPlayer().getPpNum()))));
    }
}