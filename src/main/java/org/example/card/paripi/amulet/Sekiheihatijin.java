package org.example.card.paripi.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class Sekiheihatijin extends AmuletCard {

    public Integer cost = 8;

    public String name = "石兵八阵";
    public String job = "派对咖";
    private List<String> race = Lists.ofStr("阵法");

    public String mark = """
        双方随从攻击时，己方场上每有1个随从便降低15%命中率
        """;
    public String subMark = "";

    public Sekiheihatijin() {

        addEffects((new Effect(this,this, EffectTiming.WhenOtherAttack, obj->

            // TODO
        {}
        )));
    }

}
