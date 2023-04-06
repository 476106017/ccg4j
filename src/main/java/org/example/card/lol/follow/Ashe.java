package org.example.card.lol.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Ashe extends FollowCard {
    private String name = "寒冰射手艾希";
    private Integer cost = 4;
    private int atk = 3;
    private int hp = 5;
    private String job = "英雄联盟";
    private List<String> race = Lists.ofStr();
    private String mark = """
    攻击时：如果攻击随从，使目标获得一层【冻结】
    """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("远程");
        addEffects(new Effect(this,this, EffectTiming.WhenAttack, obj->{
            Damage damage = (Damage) obj;
            if(damage.getTo() instanceof FollowCard toFollow){
                toFollow.addKeyword("冻结");
            }
        }));
    }
}