package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.ccg.hunter.follow.BattyGuest;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.Comparator;
import java.util.List;


@Getter
@Setter
public class InsatiableDevourer extends FollowCard {
    private String name = "贪食的吞噬者";
    private Integer cost = 9;
    private int atk = 4;
    private int hp = 4;
    private String job = "中立";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：吞食一个敌方随从并获得其属性值。注能（5）：随后吞食相邻随从。
        """;
    public String subMark = "注能次数：{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    public InsatiableDevourer() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this,
            EffectTiming.Charge, obj -> count())));
        setPlay(new Play(()->enemyPlayer().getAreaFollowsAsGameObj(),
            false,
            obj->{
                if(obj instanceof FollowCard followCard){
                    if(getCount()<5){
                        followCard.destroyedBy(this);
                        addStatus(followCard.getAtk(),followCard.getHp());
                    }
                    if(getCount()>=5){
                        int index = enemyPlayer().getArea().indexOf(followCard);
                        FollowCard f1=null,f2=null;
                        if(index+1 < enemyPlayer().getArea().size() &&
                            enemyPlayer().getArea().get(index+1) instanceof FollowCard followCard1){
                            f1 = followCard1;
                        }
                        if(index > 0 &&
                            enemyPlayer().getArea().get(index-1) instanceof FollowCard followCard1){
                            f2 = followCard1;
                        }
                        followCard.destroyedBy(this);
                        if(f1!=null){
                            f1.destroyedBy(this);
                        }
                        if(f2!=null) {
                            f2.destroyedBy(this);
                        }
                        addStatus(followCard.getAtk(),followCard.getHp());
                        if(f1!=null){
                            addStatus(f1.getAtk(),f1.getHp());
                        }
                        if(f2!=null) {
                            addStatus(f2.getAtk(), f2.getHp());
                        }
                    }
                }
            }
        ));
    }
}