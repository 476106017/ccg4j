package org.example.card.ccg.warrior.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class AssaultCatapult extends FollowCard {
    private String name = "突袭投石车";
    private Integer cost = 4;
    private int atk = 0;
    private int hp = 7;
    private String job = "战士";
    private List<String> race = Lists.ofStr("机械");
    private String mark = """
        回合结束时：将牌库1名随从移到墓地，对敌方英雄造成相当于其攻击力的伤害
        """;
    private String subMark = "";

    public AssaultCatapult() {
        setMaxHp(getHp());

        addEffects(new Effect(this,this, EffectTiming.EndTurn,()->{
            FollowCard deckRandomFollow = ownerPlayer().getDeckRandomFollow();
            deckRandomFollow.removeWhenNotAtArea();
            info.damageEffect(this,enemyLeader(),deckRandomFollow.getAtk());
        }));
    }
}
