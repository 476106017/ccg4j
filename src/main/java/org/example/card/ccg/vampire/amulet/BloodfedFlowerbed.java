package org.example.card.ccg.vampire.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class BloodfedFlowerbed extends AmuletCard {

    public Integer cost = 1;

    public String name = "鲜血花园";
    public String job = "吸血鬼";
    private List<String> race = Lists.ofStr();

    public String mark = """
        回合结束时：给予双方的主战者各1点伤害。
        """;
    public String subMark = "";

    List<FollowCard> effectFollows = new ArrayList<>();

    public BloodfedFlowerbed() {
        setCountDown(4);
        addEffects((new Effect(this,this,
            EffectTiming.EndTurn, obj -> info.damageMulti(this,List.of(ownerLeader(),enemyLeader()),1))));
    }

}
