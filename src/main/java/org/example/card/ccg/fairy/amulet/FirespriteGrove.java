package org.example.card.ccg.fairy.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class FirespriteGrove extends AmuletCard {

    public Integer cost = 1;

    public String name = "炎精之森";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("自然");

    public String mark = """
        回合结束时：增加1张妖精到手牌
        离场时：随机对敌方场上一名随从造成1点伤害
        """;
    public String subMark = "";

    public void init() {
        setCountDown(2);
        addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->
            ownerPlayer().addHand(createCard(Derivant.Fairy.class))
        )));

        addEffects((new Effect(this,this, EffectTiming.Leaving, obj->
        {
            final List<FollowCard> areaFollowsAsFollow = enemyPlayer().getAreaFollowsAsFollow();
            if(areaFollowsAsFollow.size()>0){
                info.damageEffect(this,Lists.randOf(areaFollowsAsFollow),1);
            }
        }
        )));
    }

}
