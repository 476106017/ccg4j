package org.example.card.ccg.necromancer.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class CursedCoin extends AmuletCard {

    public Integer cost = 1;

    public String name = "诅咒的硬币";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public int countDown = 3;

    public String mark = """
        回合结束时：死灵术 2：抽1张牌
        """;
    public String subMark = "";

    public CursedCoin() {
        addEffects((new Effect(this,this, EffectTiming.EndTurn,
            ()->  ownerPlayer().costGraveyardCountTo(2,()-> ownerPlayer().draw(1)))));
    }

}
