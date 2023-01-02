package org.example.card.fairy.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.chainsawman.equipment.ChainsawMode;
import org.example.card.fairy.follow.Fairy;
import org.example.system.Lists;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Setter
public class FirespriteGrove extends AmuletCard {

    public Integer cost = 1;

    public String name = "炎精之森";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("自然");
    public int countDown = 2;

    public String mark = """
        回合结束时：增加1张妖精到手牌
        离场时：随机对敌方场上一名随从造成1点伤害
        """;
    public String subMark = "";

    public FirespriteGrove() {
        getEffectEnds().add(new Event.EffectEnd(()->
            ownerPlayer().addHand(createCard(Fairy.class))
        ));

        getLeavings().add(new Event.Leaving(()->
            Lists.randOf(enemyPlayer().getAreaFollowsAsFollow()).damaged(this,1)
        ));
    }

}
