package org.example.card.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class NaturesGuidance extends SpellCard {
    public Integer cost = 1;
    public String name = "大自然的导引";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("自然");
    public String mark = """
        返回1张己方随从或己方护符，抽一张牌
        """;

    public String subMark = "";

    public NaturesGuidance() {
        getPlays().add(new Event.Play(
            () -> ownerPlayer().getAreaBy(areaCard -> true).stream()
                .map(areaCard -> (GameObj)areaCard).toList(),1,
            gameObjs -> {


                GameObj areaCard = gameObjs.get(0);
                assert areaCard instanceof AreaCard;

                ((AreaCard) areaCard).backToHand();
                ownerPlayer().draw(1);
            }
        ));
    }
}
