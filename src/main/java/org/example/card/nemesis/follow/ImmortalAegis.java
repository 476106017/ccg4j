package org.example.card.nemesis.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.nemesis.spell.MercurialMight;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class ImmortalAegis extends FollowCard {
    private Integer cost = 6;
    private String name = "永恒之盾·席翁";
    private String job = "复仇者";
    private List<String> race = Lists.ofStr();
    private String mark = """
        无法破坏 效果伤害免疫
        战吼：增加1张消费为0的水银的断绝到手牌
        """;
    private String subMark = "";

    private int atk = 4;
    private int hp = 8;

    public ImmortalAegis() {
        setMaxHp(getHp());
        getKeywords().add("无法破坏");
        getKeywords().add("魔法免疫");

        getPlays().add(new Card.Event.Play(ArrayList::new,0,
            gameObjs -> {
                List<Card> addCards = new ArrayList<>();
                MercurialMight mercurialMight = createCard(MercurialMight.class);
                mercurialMight.setCost(0);
                addCards.add(mercurialMight);
                ownerPlayer().addHand(addCards);
        }));
    }
}
