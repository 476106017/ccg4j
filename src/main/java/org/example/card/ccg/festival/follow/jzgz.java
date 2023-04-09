package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class jzgz extends FollowCard {
    public Integer cost = 4;
    public String name = "举烛观众";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        使相邻随从获得【圣盾】
        """;
    public String subMark = "";


    public int atk = 3;
    public int hp = 3;
    private List<FollowCard> effectedFollow = new ArrayList<>();

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("圣盾");
        setPlay(new Play(this::sh));
        addEffects((new Effect(this,this, EffectTiming.WhenSummon, this::sh)));
        addEffects((new Effect(this,this, EffectTiming.WhenDestroy, this::sh)));
        addEffects((new Effect(this,this, EffectTiming.WhenBackToHand, this::sh)));
        addEffects((new Effect(this,this, EffectTiming.WhenNoLongerAtArea, ()->{
            effectedFollow.forEach(followCard -> followCard.removeKeyword("圣盾"));
        })));
    }

    private void sh(){
        List<AreaCard> areaCopy = ownerPlayer().getAreaCopy();
        int i = areaCopy.indexOf(this);
        if(i > 0){
            AreaCard areaCard = areaCopy.get(i - 1);
            if(areaCard instanceof FollowCard followCard && !followCard.hasKeyword("圣盾")){
                effectedFollow.add(followCard);
                followCard.addKeyword("圣盾");
            }
        }
        if(i < areaCopy.size()-1){
            AreaCard areaCard = areaCopy.get(i + 1);
            if(areaCard instanceof FollowCard followCard && !followCard.hasKeyword("圣盾")){
                effectedFollow.add(followCard);
                followCard.addKeyword("圣盾");
            }
        }
    }
}
