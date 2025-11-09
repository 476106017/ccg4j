package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Bazzalan extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "巴扎兰";
    private Integer cost = 4;
    private int atk = 2;
    private int hp = 3;
    private String job = "中立";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：你对手的手牌中每有1张法术牌，便抽1张牌。
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            ownerPlayer().draw(enemyPlayer().getHandBy(p->p instanceof SpellCard).size());
        }));
    }
}
