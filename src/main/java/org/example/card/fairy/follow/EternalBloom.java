package org.example.card.fairy.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.fairy.spell.EternalForest;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class EternalBloom extends FollowCard {

    public Integer cost = 0;

    public String name = "永恒之花";
    public String job = "妖精";
    private List<String> race = List.of("植物");
    public String mark = """
        瞬念召唤：回合开始时
        离场时：摧毁己方场上所有植物，每摧毁1张，便随机破坏1张对手随从卡，并且抽1张牌
        轮回时：增加1张永恒森林到牌堆中
        突进
        """;
    public String subMark = "";

    public int atk = 0;
    public int hp = 1;

    public EternalBloom() {
        super();
        getKeywords().add("突进");

        getInvocationBegins().add(new Card.Event.InvocationBegin(
            ()->true,
            ()->{}
        ));

        getLeavings().add(new AreaCard.Event.Leaving(
            ()->{
                List<AreaCard> plants = new ArrayList<>();
                ownerPlayer().getArea().stream()
                    .filter(areaCard -> areaCard.getRace().contains("植物"))
                    .forEach(plants::add);
                plants.forEach(plantCard->{
                    plantCard.death();
                    List<AreaCard> oppositeArea = oppositePlayer().getArea();
                    oppositeArea.get((int) (oppositeArea.size() * Math.random())).death();
                    ownerPlayer().draw(1);
                });
            }
        ));

        getTransmigrations().add(new Card.Event.Transmigration(
            ()->{
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(EternalForest.class));
                ownerPlayer().addDeck(addCards);
            }
        ));

    }

}
