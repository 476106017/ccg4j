package org.example.card.stalker.follow;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;

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
    private List<String> race = List.of();
    private String mark = """
        战吼：将1名己方随从的3张复制洗入牌堆
        """;
    private String subMark = "";

    public LabRecruiter() {
        setMaxHp(getHp());
        getPlays().add(new Card.Event.Play(
            ()->ownerPlayer().getAreaFollowsAsGameObj(),1,
            gameObjs -> {
                FollowCard followCard = (FollowCard) gameObjs.get(0);
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(followCard.getClass()));
                addCards.add(createCard(followCard.getClass()));
                addCards.add(createCard(followCard.getClass()));
                ownerPlayer().addDeck(addCards);
            }));
    }
}
