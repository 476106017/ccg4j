package org.example.card.ccg.necromancer.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class GloamingTombs extends AmuletCard {

    public Integer cost = 2;

    public String name = "林立的墓地";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();

    public String mark = """
        我方召唤时：墓地+1
        """;
    public String subMark = "";

    public GloamingTombs() {
        addEffects((new Effect(this,this,
            EffectTiming.WhenSummon, obj -> ownerPlayer().countToGraveyard(1))));
    }

}
