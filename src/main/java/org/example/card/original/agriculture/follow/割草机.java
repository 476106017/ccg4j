package org.example.card.original.agriculture.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class 割草机 extends FollowCard {
    private String name = "割草机";
    private Integer cost = 3;
    private int atk = 4;
    private int hp = 5;
    private String job = "农业";
    private List<String> race = Lists.ofStr();
    private String mark = """
        受伤时：如果主战者生命大于10，则获得【冻结】，否则随机破坏一个敌方随从
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects(new Effect(this,this, EffectTiming.AfterDamaged, obj->{
            if(ownerPlayer().getHp()>10){
                addKeyword("冻结");
            }else {
                final AreaCard follow = enemyPlayer().getAreaRandomFollow();
                if(follow!=null){
                    destroy(follow);
                }
            }
        }));
    }
}