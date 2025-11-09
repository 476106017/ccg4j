package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class yyjba extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "音乐节保安";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        压轴：使所有敌方随从攻击本随从
        """;
    public String subMark = "";

    public int atk = 2;
    public int hp = 5;

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("守护");
        setPlay(new Play(()->{
            if(ownerPlayer().getPpNum()==0) {
                List<FollowCard> follows = enemyPlayer().getAreaFollowsAsFollow();
                follows.forEach(followCard -> {
                    if(atArea()) followCard.attack(this);
                });
            }
        }));
    }

}
