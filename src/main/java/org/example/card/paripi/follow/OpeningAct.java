package org.example.card.paripi.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class OpeningAct extends FollowCard {
    private String name = "开幕式表演者";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 2;
    private String job = "派对咖";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：搜索1张费用等于当前PP数的卡牌
        """;
    private String subMark = "";

    public OpeningAct() {
        setMaxHp(getHp());
        setPlay(new Play(()->
            ownerPlayer().draw(card -> card.getCost().equals(ownerPlayer().getPpNum()))));
    }
}