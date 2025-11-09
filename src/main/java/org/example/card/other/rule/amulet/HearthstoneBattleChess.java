package org.example.card.other.rule.amulet;

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
import org.example.constant.CardRarity;

@Getter
@Setter
public class HearthstoneBattleChess  extends AmuletCard {


   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;

    public String name = "酒馆战棋";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr("机械");
    public String mark = """
        若此卡在场上，双方全部随从获得【缴械】【疾驰】
        双方回合结束时，双方未攻击随从轮流发起攻击
        """;
    public String subMark = "";
    List<FollowCard> effectFollows = new ArrayList<>();


    public void init() {
        addEffects((new Effect(this,this, EffectTiming.WhenAtArea, obj->{
            ownerPlayer().getAreaFollowsAsFollow().forEach(this::jx);
            enemyPlayer().getAreaFollowsAsFollow().forEach(this::jx);
        })));
        addEffects((new Effect(this,this,
            EffectTiming.WhenSummon,areaCard -> jx((List<AreaCard>)areaCard))));
        addEffects((new Effect(this,this,
            EffectTiming.WhenEnemySummon,areaCard -> jx((List<AreaCard>)areaCard))));
        addEffects((new Effect(this,this, EffectTiming.WhenNoLongerAtArea, obj->
            effectFollows.forEach(((followCard) -> followCard.removeKeyword("缴械")))
        )));

        addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->autoBattle())));
        addEffects((new Effect(this,this, EffectTiming.EnemyEndTurn, obj->autoBattle())));
    }

    private void jx(List<AreaCard> areaCards){
        areaCards.forEach(this::jx);
    }
    private void jx(AreaCard areaCard){
        if(areaCard instanceof FollowCard followCard){
            followCard.addKeyword("缴械");
            followCard.addKeyword("疾驰");
            effectFollows.add(followCard);
        }
    }

    private void autoBattle() {
        while (true) {
            boolean end = true;
            // 获取还能攻击的随从（未达到攻击次数上限）
            List<FollowCard> thisFollows = info.thisPlayer().getAreaFollowsAsFollowBy(
                f -> f.getTurnAttack() < f.getTurnAttackMax()
            );
            if (!thisFollows.isEmpty()) {
                end = false;

                List<FollowCard> canAttackFollows = info.oppositePlayer().getAreaCanAttackFollows();
                if (!canAttackFollows.isEmpty())
                    thisFollows.get(0).attack(canAttackFollows.get(0));
                else
                    thisFollows.get(0).attack(info.oppositePlayer().getLeader());

            }

            // 获取还能攻击的随从（未达到攻击次数上限）
            List<FollowCard> thatFollows =  info.oppositePlayer().getAreaFollowsAsFollowBy(
                f -> f.getTurnAttack() < f.getTurnAttackMax()
            );
            if (!thatFollows.isEmpty()) {
                end = false;

                List<FollowCard> canAttackFollows =  info.thisPlayer().getAreaCanAttackFollows();
                if (!canAttackFollows.isEmpty())
                    thatFollows.get(0).attack(canAttackFollows.get(0));
                else
                    thatFollows.get(0).attack(info.thisPlayer().getLeader());
            }

            if (end) return;
        }
    }
}
