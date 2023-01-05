package org.example.card.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class AirboundBarrage extends SpellCard {
    public Integer cost = 1;
    public String name = "对空射击";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        返回1张我方随从或我方护符，随机对敌方场上1名随从造成3点伤害
        """;

    public String subMark = "";

    public AirboundBarrage() {
        setPlay(new Play(
            () -> ownerPlayer().getAreaBy(areaCard -> true).stream()
                .map(areaCard -> (GameObj)areaCard).toList(),true,
            gameObjs -> {
                ((AreaCard) gameObjs).backToHand();
                info.damageEffect(this, Lists.randOf(enemyPlayer().getAreaFollowsAsFollow()),3);
            }));
    }
}
