package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Blightbearer extends FollowCard {
    private String name = "荒芜传疫者";
    private Integer cost = 5;
    private int atk = 4;
    private int hp = 4;
    private String job = "死灵法师";
    private List<String> race = Lists.ofStr();
    private String mark = """
        亡语：使场上1个随机随从获得该亡语，并且-2/-2
        """;
    private String subMark = "";
    private transient Effect theDeathRattle;

    public void init() {
        setMaxHp(getHp());

        theDeathRattle = new Effect(this, this, EffectTiming.DeathRattle, () -> {
            AreaCard areaCard = Lists.randOf(info.getAreaFollowsCopy());
            FollowCard followCard = (FollowCard) areaCard;
            followCard.addEffects(theDeathRattle);
            followCard.addStatus(-2,-2);
        });
        addEffects(theDeathRattle);
    }
}
