package org.example.card.stalker.spell.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class Shadowstep extends SpellCard {
    public Integer cost = 0;
    public String name = "暗影步";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        返回1张己方随从，使其费用-2
        """;

    public String subMark = "";

    public Shadowstep() {
        getPlays().add(new Event.Play(
            () -> ownerPlayer().getAreaFollowsAsGameObj(),1,
            gameObjs -> {
                AreaCard areaCard = (AreaCard)gameObjs.get(0);
                areaCard.backToHand();
                int newCost = areaCard.getCost()-2;
                areaCard.setCost(Math.max(newCost, 0));
            }
        ));
    }
}
