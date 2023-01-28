package org.example.card.ccg.paladin.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class CowardlyKnight extends FollowCard {
    private String name = "怂骑士";
    private Integer cost = 1;
    private int atk = 0;
    private int hp = 5;
    private String job = "圣骑士";
    private List<String> race = Lists.ofStr("人类");
    private String mark = """
        受伤时：如果没有死亡，则返回手牌
        """;
    private String subMark = "";

    public CowardlyKnight() {
        setMaxHp(getHp());
        getKeywords().add("守护");
        addEffects(new Effect(this,this, EffectTiming.AfterDamaged,obj->{
            if(atArea() && getHp()>0){
                backToHand();
            }
        }));
    }
}
