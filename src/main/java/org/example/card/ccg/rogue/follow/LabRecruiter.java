package org.example.card.ccg.rogue.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class LabRecruiter extends FollowCard {
    private String name = "实验室招募员";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 2;
    private String job = "潜行者";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：将1名我方随从的3张复制洗入牌堆
        """;
    private String subMark = "";

    public LabRecruiter() {
        setMaxHp(getHp());
        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObj(),true,
            gameObjs -> {
                FollowCard followCard = (FollowCard) gameObjs;
                List<Card> addCards = new ArrayList<>();

                addCards.add(followCard.clone());
                addCards.add(followCard.clone());
                addCards.add(followCard.clone());

                ownerPlayer().addDeck(addCards);
            }));
    }
}
