package org.example.card.ccg.nemesis.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class AnalyzingArtifact  extends FollowCard {
    private String name = "解析的造物";
    private Integer cost = 1;
    private int atk = 2;
    private int hp = 1;
    private String job = "复仇者";
    private List<String> race = Lists.ofStr("创造物");
    private String mark = """
        亡语：抽1张牌
        """;
    private String subMark = "";


    public AnalyzingArtifact() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            ownerPlayer().draw(1);
        })));
    }
}
