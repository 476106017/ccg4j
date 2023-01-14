package org.example.card.genshin;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.EquipmentCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.GameObj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.ALL_COST;
import static org.example.constant.CounterKey.PLAY_NUM;

@Getter
@Setter
public abstract class ElementCostSpellCard extends SpellCard {
    Integer cost = 0;
    List<Elemental> elementCost;

    public void play(List<GameObj> targets, int choice){
        if(!(ownerPlayer().getLeader() instanceof LittlePrincess littlePrincess)){
            info.msgToThisPlayer("请使用主战者【小王子】来打出此牌！");
            return;
        }

        List<Elemental> elementCostCopy = new ArrayList<>(getElementCost());

        // region 检查无元素是否满足
        int costVoidCount = (int) elementCostCopy.stream()
            .filter(elemental -> elemental == Elemental.Void).count();

        int ppNum = ownerPlayer().getPpNum();
        int usePP = 0;
        if(ppNum < costVoidCount){
            // pp不够用的情况
            usePP = ppNum;
        }else {
            // pp够用的情况
            usePP = costVoidCount;
        }
        for (int i = 0; i < usePP; i++) {
            // 每个用掉的PP都抵消一个无元素消耗
            elementCostCopy.remove(Elemental.Void);
        }
        // endregion

        // region 转换消耗中的主元素
        Elemental mainElemental;
        if(this instanceof NormalAttack){
            List<AreaCard> guards = ownerPlayer().getAreaFollowsBy(followCard ->
                followCard instanceof ElementBaseFollowCard elementBaseFollowCard && elementBaseFollowCard.hasKeyword("守护"));
            if(guards.isEmpty()){
                info.msg("没有可以攻击的随从，技能没有任何效果！");
                return;
            }
            ElementBaseFollowCard fromFollow = (ElementBaseFollowCard) guards.get(0);
            mainElemental = fromFollow.getElement();
        }else {// 普攻以外，都是取创造该技能的随从
            mainElemental = ((ElementBaseFollowCard)getParent()).getElement();
        }

        long main = elementCostCopy.stream()
            .filter(elemental -> elemental == Elemental.Main).count();
        for (long i = 0; i < main; i++) {
            elementCostCopy.remove(Elemental.Main);
            elementCostCopy.add(mainElemental);
        }
        // endregion

        if(!littlePrincess.hasDices(elementCostCopy)){
            info.msgToThisPlayer("缺少满足条件元素骰！使用卡牌失败");
            return;
        }

        info.msg(ownerPlayer().getName() + "使用了技能：" + getName());

        // region 在使用卡牌造成任何影响前，先计算使用时
        ownerPlayer().getLeader().useEffects(EffectTiming.WhenPlay,this);
        enemyPlayer().getLeader().useEffects(EffectTiming.WhenEnemyPlay,this);
        ownerPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.WhenPlay,this));
        enemyPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.WhenEnemyPlay,this));
        // endregion 在使用卡牌造成任何影响前，先计算使用时

        // region 发动卡牌效果
        if(getPlay() != null){
            // 如果必须传目标，没有可选择目标时不发动效果
            if(getPlay().mustTarget() && targets.isEmpty()){
                info.msg(getNameWithOwner() + "因为没有目标而无法发动效果！");
            }else {
                getPlay().effect().accept(choice,targets);
            }
        }
        // endregion 发动卡牌效果

        // 触发手牌上全部增幅效果
        String boostCards = ownerPlayer().getHandCopy().stream().map(card -> card.getEffects(EffectTiming.Boost))
            .flatMap(Collection::stream)
            .filter(boost -> boost.getCanEffect().test(this))
            .map(effect -> effect.getOwnerObj().getId()).collect(Collectors.joining("、"));
        if(!boostCards.isEmpty()){
            info.msgToThisPlayer(boostCards + "发动增幅效果");
        }

        ownerPlayer().count(PLAY_NUM);

        info.startEffect();

        info.msgTo(ownerPlayer().getUuid(),
            info.describeArea(ownerPlayer().getUuid()) + ownerPlayer().describePPNum());
        info.msgTo(enemyPlayer().getUuid(),
            info.describeArea(enemyPlayer().getUuid()) + ownerPlayer().describePPNum());
    }
}
