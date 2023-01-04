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
public class PixieMischief extends SpellCard {
    public Integer cost = 2;
    public String name = "妖精的恶作剧";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        返回1张我方随从或我方护符，随机返回敌方场上1名随从
        """;

    public String subMark = "";

    public PixieMischief() {
        setPlay(new Play(
            () -> ownerPlayer().getAreaBy(areaCard -> true).stream()
                .map(areaCard -> (GameObj)areaCard).toList(),1,
            gameObjs -> {
                GameObj areaCard = gameObjs.get(0);
                ((AreaCard) areaCard).backToHand();
                Lists.randOf(enemyPlayer().getAreaFollowsAsFollow()).backToHand();
            }
        ));
    }
}
