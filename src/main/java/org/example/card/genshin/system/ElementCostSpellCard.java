package org.example.card.genshin.system;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.card.genshin.LittlePrincess;
import org.example.constant.EffectTiming;
import org.example.game.GameObj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.PLAY_NUM;

@Getter
@Setter
public abstract class ElementCostSpellCard extends SpellCard {
    Integer cost = 0;
    List<Elemental> elementCost;

    public ElementBaseFollowCard activeFollow(){
        List<AreaCard> guards = ownerPlayer().getAreaFollowsBy(followCard ->
            followCard instanceof ElementBaseFollowCard elementBaseFollowCard && elementBaseFollowCard.hasKeyword("守护"));
        if(guards.isEmpty()){
            return null;
        }
        return (ElementBaseFollowCard) guards.get(0);
    }

    public void play(List<GameObj> targets, int choice){
        if(!(ownerPlayer().getLeader() instanceof LittlePrincess littlePrincess)){
            info.msgToThisPlayer("请使用主战者【小王子】来打出此牌！");
            return;
        }
        ElementBaseFollowCard activeFollow = activeFollow();
        if(getParent() instanceof ElementBaseFollowCard && getParent()!=activeFollow){
            // 如果是由元素随从创造的技能牌，创造者必须是当前上场随从
            info.msgToThisPlayer(getParent().getName()+"不是出战角色，无法使用该技能！");
            return;
        }

        List<Elemental> elementCostCopy = new ArrayList<>(getElementCost());

        // region 检查无元素是否满足
        int costVoidCount = (int) elementCostCopy.stream()
            .filter(elemental -> elemental == Elemental.Void).count();

        int ppNum = ownerPlayer().getPpNum();
        int usePP;
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
        long main = elementCostCopy.stream()
            .filter(elemental -> elemental == Elemental.Main).count();
        if(main>0){
            if(activeFollow == null){
                info.msg("没有出战中的随从，技能没有任何效果！");
                return;
            }
            for (long i = 0; i < main; i++) {
                elementCostCopy.remove(Elemental.Main);
                elementCostCopy.add(activeFollow.getElement());
            }
        }
        // endregion

        if(!littlePrincess.hasDices(elementCostCopy)){
            info.msgToThisPlayer("缺少满足条件元素骰！使用卡牌失败");
            return;
        }

        if (hasRace("行动")) {
            info.msg(ownerPlayer().getName() + "使用行动：" + getName());
        } else {
            info.msg(ownerPlayer().getName() + "使用了：" + getName());
            ownerPlayer().getGraveyard().add(this);
            ownerPlayer().countToGraveyard(1);
            ownerPlayer().getHand().remove(this);
        }
        // region 实际扣费
        ownerPlayer().setPpNum(ppNum - usePP);
        littlePrincess.useDices(elementCostCopy);
        // endregion 实际扣费

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
