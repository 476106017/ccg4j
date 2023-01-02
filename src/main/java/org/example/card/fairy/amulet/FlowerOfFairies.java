package org.example.card.fairy.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.fairy.follow.Fairy;
import org.example.system.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
public class FlowerOfFairies extends AmuletCard {

    public Integer cost = 2;

    public String name = "妖精之花";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("自然");
    public int countDown = 2;

    public String mark = """
        战吼：抽1张牌
        亡语/返回手牌时：增加1张妖精萤火到手牌
        """;
    public String subMark = "";

    public FlowerOfFairies() {
        getPlays().add(new Card.Event.Play(()->{
            ownerPlayer().draw(1);
        }));
        getDeathRattles().add(new Event.DeathRattle(()->
            ownerPlayer().addHand(createCard(Fairy.class))
        ));
        getWhenBackToHands().add(new Event.WhenBackToHand(()->
            ownerPlayer().addHand(createCard(Fairy.class))
        ));
    }

}
