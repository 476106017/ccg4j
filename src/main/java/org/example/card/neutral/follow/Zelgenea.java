package org.example.card.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Play;
import org.example.system.Lists;

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
        getInvocationBegins().add(new Card.Event.InvocationBegin(
            ()-> info.getTurn() == 10,
            ()->{
                addStatus(5,5);
                addKeyword("突进");
                getWhenAttacks().add(new Event.WhenAttack(damage -> {
                    ownerPlayer().getLeader().addEffect(this, EffectTiming.EndTurn,()->{
                        // 主战者扣血
                        ownerPlayer().getLeader().damaged(this,4);
                        enemyPlayer().getLeader().damaged(this,4);
                        // 敌方随从扣血
                        enemyPlayer().getAreaFollowsAsFollow()
                            .forEach(followCard -> {
                                Damage dmg = new Damage(this, followCard, 4);
                                followCard.damagedWithoutSettle(dmg);
                            });
                        // 己方随从扣血
                        ownerPlayer().getAreaFollowsAsFollow()
                            .forEach(followCard -> {
                                Damage dmg = new Damage(this, followCard, 4);
                                followCard.damagedWithoutSettle(dmg);
                            });
                        // 敌方随从结算
                        enemyPlayer().getAreaFollowsAsFollow()
                            .forEach(followCard -> {
                                Damage dmg = new Damage(this, followCard, 4);
                                followCard.damageSettlement(dmg);
                            });
                        // 己方随从结算
                        ownerPlayer().getAreaFollowsAsFollow()
                            .forEach(followCard -> {
                                Damage dmg = new Damage(this, followCard, 4);
                                followCard.damageSettlement(dmg);
                            });
                    } );
                }));
            }
        ));
    }
}
