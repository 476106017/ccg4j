package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.ccg.hunter.Rexxar;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Aralon extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "艾拉隆";
    private Integer cost = 5;
    private int atk = 4;
    private int hp = 5;
    private String job = "猎人";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：召唤每种灵种各1个
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()-> {
            ownerPlayer().summon(createCard(Rexxar.FoxSpiritWildseed.class));
            ownerPlayer().summon(createCard(Rexxar.BearSpiritWildseed.class));
            ownerPlayer().summon(createCard(Rexxar.StagSpiritWildseed.class));
        }));
    }

}
