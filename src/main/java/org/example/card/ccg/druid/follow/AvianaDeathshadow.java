package org.example.card.ccg.druid.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class AvianaDeathshadow extends FollowCard {
    private String name = "死亡之影艾维娜";
    private Integer cost = 5;
    private int atk = 7;
    private int hp = 3;
    private String job = "德鲁伊";
    private List<String> race = Lists.ofStr();
    private String mark = """
        入场时：如果牌堆没有卡牌，手牌费用全部变为0
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this,
            EffectTiming.Entering, () -> {
            if(ownerPlayer().getDeck().isEmpty()) {
                ownerPlayer().getHand().forEach(card -> card.setCost(0));
            }
        })));
    }
}