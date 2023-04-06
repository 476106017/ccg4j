package org.example.card.ccg.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

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

    public void init() {
        setPlay(new Play(
            () -> ownerPlayer().getAreaBy(areaCard -> true).stream()
                .map(areaCard -> (GameObj)areaCard).toList(),true,
            gameObj -> {
                ((AreaCard) gameObj).backToHand();
                Lists.randOf(enemyPlayer().getAreaFollowsAsFollow()).backToHand();
            }));
    }
}
