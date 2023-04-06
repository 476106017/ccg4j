package org.example.card.ccg.hunter.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.card.ccg.hunter.follow.BattyGuest;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class FrenziedFangs extends SpellCard {
    public Integer cost = 2;
    public String name = "狂暴利齿";
    public String job = "猎人";
    private List<String> race = Lists.ofStr();
    public String mark = """
        召唤2只2/1的蝙蝠
        注能(3)：使其获得+1/+2
        """;

    public String subMark = "注能次数：{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    public void init() {
        addEffects((new Effect(this,this,
            EffectTiming.Charge, obj -> count())));
        setPlay(new Play(
            ()->{
                BattyGuest.ThirstyBat bat1 = createCard(BattyGuest.ThirstyBat.class);
                BattyGuest.ThirstyBat bat2 = createCard(BattyGuest.ThirstyBat.class);
                if(getCount()>=3){
                    bat1.addStatus(1,2);
                    bat2.addStatus(1,2);
                }
                ownerPlayer().summon(bat1);
                ownerPlayer().summon(bat2);
            }
        ));
    }
}
