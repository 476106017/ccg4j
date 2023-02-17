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
public class MalignantHorror extends FollowCard {
    private String name = "恶毒恐魔";
    private Integer cost = 4;
    private int atk = 2;
    private int hp = 4;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        回合结束时：死灵术 4：召唤1个该随从的复制
        """;
    private String subMark = "";

    public MalignantHorror() {
        setMaxHp(getHp());
        getKeywords().add("复生");
        addEffects((new Effect(this,this,
            EffectTiming.EndTurn, obj -> {
            ownerPlayer().costGraveyardCountTo(4,()-> ownerPlayer().summon((AreaCard) clone()));
        })));
    }
}