package org.example.card.nemesis.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class WorldEliminator extends FollowCard {
    private Integer cost = 5;
    private String name = "世界驱除者";
    private String job = "复仇者";

    private List<String> race = new ArrayList<>();
    private String mark = """
        亡语：使自己主战者生命最大值+2，回复2点生命
        """;
    private String subMark = "";

    private int atk = 3;
    private int hp = 3;

    public WorldEliminator() {
        setMaxHp(getHp());
        getKeywords().add("突进");
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            ownerPlayer().addHpMax(2);
            ownerPlayer().heal(2);
        })));
    }

}
