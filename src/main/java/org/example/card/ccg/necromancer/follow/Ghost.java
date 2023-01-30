package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.ccg.fairy.follow.Fairy;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class Ghost extends FollowCard {
    private String name = "怨灵";
    private Integer cost = 0;
    private int atk = 1;
    private int hp = 1;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        回合结束时：死亡
        """;
    private String subMark = "";

    public Ghost() {
        setMaxHp(getHp());
        getKeywords().add("疾驰");
        getKeywords().add("游魂");

        addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->
            death()
        )));
    }
}