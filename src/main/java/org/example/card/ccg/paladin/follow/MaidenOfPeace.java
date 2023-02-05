package org.example.card.ccg.paladin.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class MaidenOfPeace extends FollowCard {
    private String name = "和平圣女";
    private Integer cost = 3;
    private int atk = 0;
    private int hp = 5;
    private String job = "圣骑士";
    private List<String> race = Lists.ofStr("人类");
    private String mark = """
        亡语：结束进行中的回合
        """;
    private String subMark = "";

    public MaidenOfPeace() {
        setMaxHp(getHp());
        getKeywords().add("守护");
        addEffects(new Effect(this,this, EffectTiming.DeathRattle,()->{
            info.endTurnOfCommand();
        }));
    }
}
