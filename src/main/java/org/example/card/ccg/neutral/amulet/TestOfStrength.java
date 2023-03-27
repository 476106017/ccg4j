package org.example.card.ccg.neutral.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class TestOfStrength extends AmuletCard {

    public Integer cost = 3;

    public String name = "阿秋霸";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public transient int countDown = 2;

    public String mark = """
        若此卡在场上，双方全部随从获得【守护】
        """;
    public String subMark = "";

    List<FollowCard> effectFollows = new ArrayList<>();

    public TestOfStrength() {
        addEffects((new Effect(this,this, EffectTiming.WhenAtArea, obj->{
            ownerPlayer().getAreaFollowsAsFollow().forEach(this::sh);
            enemyPlayer().getAreaFollowsAsFollow().forEach(this::sh);
        })));
        addEffects((new Effect(this,this,
            EffectTiming.WhenSummon,areaCard -> sh(((List<AreaCard>)areaCard)))));
        addEffects((new Effect(this,this,
            EffectTiming.WhenEnemySummon,areaCard -> sh(((List<AreaCard>)areaCard)))));
        addEffects((new Effect(this,this, EffectTiming.WhenNoLongerAtArea, obj->
            effectFollows.forEach(((followCard) -> followCard.removeKeyword("守护")))
        )));
    }

    private void sh(List<AreaCard> areaCards){
        areaCards.forEach(this::sh);
    }
    private void sh(AreaCard areaCard){
        if(areaCard instanceof FollowCard followCard){
            followCard.addKeyword("守护");
            effectFollows.add(followCard);
        }
    }
}
