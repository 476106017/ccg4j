package org.example.card.ccg.rogue.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Shadowstep extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 0;
    public String name = "暗影步";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        返回1张我方随从，使其费用-2
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(
            () -> ownerPlayer().getAreaFollowsAsGameObj(),true,
            gameObj -> {
                AreaCard areaCard = (AreaCard)gameObj;
                areaCard.backToHand();
                int newCost = areaCard.getCost()-2;
                areaCard.setCost(Math.max(newCost, 0));
            }));
    }
}
