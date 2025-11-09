package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class PetCollector extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "宠物收集者";
    private Integer cost = 5;
    private int atk = 4;
    private int hp = 4;
    private String job = "猎人";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：招募1只费用5以下的野兽
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()-> {
            ownerPlayer().hire(card -> card instanceof FollowCard && card.hasRace("野兽") && card.getCost()<=5);
        }));
    }

}
