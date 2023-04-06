package org.example.card.ccg.necromancer.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class HauntedHouse extends AmuletCard {

    public Integer cost = 1;

    public String name = "幽灵宅邸";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();

    public String mark = """
        发动死灵术时：召唤1个怨灵
        """;
    public String subMark = "";

    public void init() {
        setCountDown(3);
        addEffects((new Effect(this,this,
            EffectTiming.WhenCostGraveyard, obj -> ownerPlayer().summon(createCard(Derivant.Ghost.class)))));
    }

}
