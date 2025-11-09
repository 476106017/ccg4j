package org.example.card.ccg.vampire.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class AmblingWraith extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "SV影魔";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "吸血鬼";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：给予双方的主战者各1点伤害。
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());

        setPlay(new Play(()-> info.damageMulti(this,List.of(ownerLeader(),enemyLeader()),1)));
    }
}
