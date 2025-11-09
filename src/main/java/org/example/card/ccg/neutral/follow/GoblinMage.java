package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class GoblinMage extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "迷你哥布林法师";
    private Integer cost = 3;
    private int atk = 2;
    private int hp = 2;
    private String job = "中立";
    private List<String> race = Lists.ofStr("人类");
    private String mark = """
        战吼：搜索1张费用为2的随从
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->
            ownerPlayer().draw(card -> card.getCost().equals(2))));
    }
}
