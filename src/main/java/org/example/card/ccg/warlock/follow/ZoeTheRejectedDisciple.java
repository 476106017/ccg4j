package org.example.card.ccg.warlock.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class ZoeTheRejectedDisciple extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "佐伊·被回绝的信徒";
    private Integer cost = 8;
    private int atk = 3;
    private int hp = 5;
    private String job = "术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：和你的对手交换疲劳伤害
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            int weary = ownerPlayer().getWeary();
            ownerPlayer().setWeary(enemyPlayer().getWeary());
            enemyPlayer().setWeary(weary);
        }));

    }
}
