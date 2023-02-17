package org.example.card.ccg.paladin.follow;

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
public class DarkIronJustice extends FollowCard {
    private String name = "黑铁裁决者";
    private Integer cost = 5;
    private int atk = 3;
    private int hp = 5;
    private String job = "圣骑士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        敌方召唤时：如果召唤的是生命值5以上的随从，则将其生命值降为1
        """;
    private String subMark = "";

    public DarkIronJustice() {
        setMaxHp(getHp());
        addEffects(new Effect(this,this, EffectTiming.WhenEnemySummon,
            obj-> {
                List<AreaCard> areaCards = (List<AreaCard>) obj;
                return areaCards.stream()
                    .anyMatch(areaCard -> areaCard instanceof FollowCard followCard && followCard.getHp()>=5);
            },
            obj->{
                List<AreaCard> areaCards = (List<AreaCard>) obj;
                areaCards.stream()
                    .filter(areaCard -> areaCard instanceof FollowCard followCard && followCard.getHp()>=5)
                    .forEach(areaCard -> ((FollowCard)areaCard).setHp(1));
        }));
    }
}
