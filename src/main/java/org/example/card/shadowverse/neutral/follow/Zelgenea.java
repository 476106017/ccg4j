package org.example.card.shadowverse.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Getter
@Setter
public class Zelgenea extends FollowCard {
    public Integer cost = 5;
    public int atk = 5;
    public int hp = 5;
    public String name = "《世界》捷尔加内亚";
    public String job = "中立";
    private List<String> race = Lists.ofStr("神");
    public String mark = """
        瞬念召唤：第10回合开始时。获得+5/+5、突进、攻击时：主战者获得唯一效果【回合结束时，对双方主战者和所有随从造成4点伤害】
        —————————————
        战吼：回复主战者5点生命。如果回复前血量在15以下，则抽2张牌、破坏对手场上攻击力最高的1张牌。
        """;
    public String subMark = "当前是第{}回合";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",info.getTurn()+"");
    }


    public Zelgenea() {
        setMaxHp(getHp());
        setPlay(new Play(() -> {
            int oldHp = ownerPlayer().getHp();
            ownerPlayer().heal(5);
            if(oldHp<15){
                ownerPlayer().draw(2);
                enemyPlayer().getAreaFollowsAsFollow().stream()
                    .max(Comparator.comparing(FollowCard::getAtk))
                    .ifPresent(followCard -> followCard.destroyedBy(this));
            }
        }));
        addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
            ()-> info.getTurn() == 10,
            ()->{
                addStatus(5,5);
                addKeyword("突进");
                addEffects((new Effect(this,this, EffectTiming.WhenAttack,damage -> {
                    ownerPlayer().getLeader().addEffect(new Effect(this, ownerPlayer().getLeader(), EffectTiming.EndTurn,()->{
                        List<GameObj> objs = new ArrayList<>();
                        objs.add(ownerPlayer().getLeader());
                        objs.add(enemyPlayer().getLeader());
                        objs.addAll(enemyPlayer().getAreaFollows());
                        objs.addAll(ownerPlayer().getAreaFollows());
                        info.damageMulti(ownerPlayer().getLeader(),objs,4);
                    } ),true);
                })));
            }
        )));
    }
}
