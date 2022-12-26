package org.example.card.fairy.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.AreaCard;
import org.example.card.fairy.amulet.EternalGarden;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class EternalSeedling extends FollowCard {

    public Integer cost = 0;

    public String name = "永恒树苗";
    public String job = "妖精";
    private List<String> race = List.of("植物");
    public String mark = """
        瞬念召唤：回合开始时
        离场时：增加1张永恒树苗到牌堆中；如果墓地中的永恒树苗数量大于3，且场上没有永恒庭园，则召唤一个永恒庭园到场上
        轮回时：增加1张永恒之花到牌堆中
        """;
    public String subMark = "墓地中的永恒树苗数量:{count}";

    public int atk = 0;
    public int hp = 1;
    public int maxHp = 1;

    public String getSubMark() {
        long count = ownerPlayer().getGraveyard().stream()
            .filter(card -> card instanceof EternalSeedling)
            .count();

        return subMark.replaceAll("\\{count}", count+"");
    }

    public EternalSeedling() {
        getInvocationBegins().add(new Card.Event.InvocationBegin(
            ()->true,
            ()->{}
        ));


        getLeavings().add(new AreaCard.Event.Leaving(
            ()->{
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(EternalSeedling.class));
                ownerPlayer().addDeck(addCards);

                // 墓地中的永恒树苗数量大于3，且场上没有永恒庭园
                long count = ownerPlayer().getGraveyard().stream()
                    .filter(card -> card instanceof EternalSeedling)
                    .count();
                if(count >= 3 && ownerPlayer().getArea().stream()
                    .filter(areaCard->areaCard instanceof EternalGarden).findAny().isEmpty()){
                    ownerPlayer().summon(createCard(EternalGarden.class));
                }
            }
        ));

        getTransmigrations().add(new Card.Event.Transmigration(
            ()->{
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(EternalBloom.class));
                ownerPlayer().addDeck(addCards);
            }
        ));
    }

}
