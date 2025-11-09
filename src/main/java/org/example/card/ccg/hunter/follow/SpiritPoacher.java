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
public class SpiritPoacher extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "灵体偷猎者";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 2;
    private String job = "猎人";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：随机召唤1个灵种
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            switch ((int)(Math.random()*3)){
                case 0 -> ownerPlayer().summon(createCard(Rexxar.FoxSpiritWildseed.class));
                case 1 -> ownerPlayer().summon(createCard(Rexxar.BearSpiritWildseed.class));
                case 2 -> ownerPlayer().summon(createCard(Rexxar.StagSpiritWildseed.class));
            }
        }));
    }

}
