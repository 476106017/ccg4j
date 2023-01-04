package org.example.card.neutral.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class TestOfStrength extends AmuletCard {

    public Integer cost = 3;

    public String name = "阿秋霸";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public int countDown = 2;

    public String mark = """
        若此卡在场上，双方全部随从获得【守护】
        """;
    public String subMark = "";

    List<FollowCard> effectFollows = new ArrayList<>();

    public TestOfStrength() {
        getEffects().add(new Effect(this,this, EffectTiming.WhenAtArea,)->{
            ownerPlayer().getAreaFollowsAsFollow().forEach(followCard -> {
                followCard.addKeyword("守护");
                effectFollows.add(followCard);
            });
            enemyPlayer().getAreaFollowsAsFollow().forEach(followCard -> {
                followCard.addKeyword("守护");
                effectFollows.add(followCard);
            });
        }));
        getEffects().add(new Effect(this,this, EffectTiming.WhenSummon,areaCard -> {
            if(areaCard instanceof FollowCard followCard){
                followCard.addKeyword("守护");
                effectFollows.add(followCard);
            }
        }));
        getEffects().add(new Effect(this,this, EffectTiming.WhenEnemySummon,areaCard -> {
            if(areaCard instanceof FollowCard followCard){
                followCard.addKeyword("守护");
                effectFollows.add(followCard);
            }
        }));
        getEffects().add(new Effect(this,this, EffectTiming.WhenNoLongerAtArea,)->
            effectFollows.forEach(((followCard) -> followCard.removeKeyword("守护")))
        ));
    }

}
