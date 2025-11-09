package org.example.card.other.rule.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.PlayerInfo;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.constant.CardRarity;

@Getter
@Setter
public class MahjongTable extends AmuletCard {


   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 7;

    public String name = "自动麻将桌";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr("机械");
    public String mark = """
        双方抽牌时：
        如果手牌费用符合门清自摸和，则获得游戏胜利
        ——
        双方使用卡牌时：
        如果使用的是随从或者护符、且场上还有空间，对方以召唤牌的费用为参考，从手牌进行鸣牌并召唤到场上
        """;
    public String subMark = "";


    public void init() {
        addEffects((new Effect(this,this, EffectTiming.WhenPlay, areaCard -> checkSummon(enemyPlayer(),(Card)areaCard))));
        addEffects((new Effect(this,this, EffectTiming.WhenEnemyPlay,areaCard -> checkSummon(ownerPlayer(),(Card)areaCard))));
        addEffects((new Effect(this,this, EffectTiming.WhenDraw, obj -> checkWin(ownerPlayer()))));
        addEffects((new Effect(this,this, EffectTiming.WhenEnemyDraw, obj -> checkWin(ownerPlayer()))));
    }

    private void checkSummon(PlayerInfo player, Card playCard){
        if(!(playCard instanceof AreaCard areaCard))return;
        if(player.getArea().size() == player.getAreaMax()) return;
        Integer theCost = areaCard.getCost();
        Map<Integer, List<AreaCard>> costGroup = player.getHand().stream()
            .filter(card -> card instanceof AreaCard)
            .map(card -> (AreaCard)card)
            .collect(Collectors.groupingBy(Card::getCost));

        // region 检查相同费用的卡牌
        List<AreaCard> sameCostCards = costGroup.get(theCost);
        if(sameCostCards!=null && sameCostCards.size() >= 2){
            // 多余3张就只取3张
            if(sameCostCards.size() > 3){
                info.msg(player.getName()+"：杠！");
                sameCostCards = sameCostCards.subList(0,3);
            }else {
                info.msg(player.getName()+"：碰!");
            }

            sameCostCards.forEach(card -> {
                if(card.atHand()){
                    card.removeWhenNotAtArea();
                    player.summon(card);
                }
            });
            return;
        }
        // endregion

        // region 检查费用能成数列的卡牌
        if(costGroup.get(theCost-2)!=null && costGroup.get(theCost-1)!=null){
            AreaCard card1 = costGroup.get(theCost - 2).get(0);
            card1.removeWhenNotAtArea();
            player.summon(card1);
            AreaCard card2 = costGroup.get(theCost - 1).get(0);
            card2.removeWhenNotAtArea();
            player.summon(card2);
            info.msg(player.getName()+"：吃!");
            return;
        }
        if(costGroup.get(theCost-1)!=null && costGroup.get(theCost+1)!=null){
            AreaCard card1 = costGroup.get(theCost - 1).get(0);
            card1.removeWhenNotAtArea();
            player.summon(card1);
            AreaCard card2 = costGroup.get(theCost + 1).get(0);
            card2.removeWhenNotAtArea();
            player.summon(card2);
            info.msg(player.getName()+"：吃!");
            return;
        }
        if(costGroup.get(theCost+1)!=null && costGroup.get(theCost+2)!=null){
            AreaCard card1 = costGroup.get(theCost + 1).get(0);
            card1.removeWhenNotAtArea();
            player.summon(card1);
            AreaCard card2 = costGroup.get(theCost + 2).get(0);
            card2.removeWhenNotAtArea();
            player.summon(card2);
            info.msg(player.getName()+"：吃!");
            return;
        }
        // endregion
    }


    private void checkWin(PlayerInfo player){
        List<Card> hand = player.getHand();// 仅检查，不需要copy
        if(hand.size() % 3 != 2)return;

        Map<Integer, Long> costCount = hand.stream()
            .collect(Collectors.groupingBy(Card::getCost, Collectors.counting()));

        List<Integer> costs = costCount.keySet().stream().sorted().toList();

        boolean isWin = checkWinLoop(costCount, costs, false);

        if (isWin) {
            // 记录详细的胜利信息
            String handInfo = hand.stream()
                .map(card -> String.format("%s(%d费)", card.getName(), card.getCost()))
                .collect(Collectors.joining(", "));
            info.addBattleLog(String.format("【麻将桌】%s 胡牌！手牌：%s", player.getName(), handInfo));
            info.setEndReason("special");
            info.msg(String.format("🀄️ %s 达成门清自摸和！", player.getName()));
            info.gameset(player);
        }
    }

    private static boolean checkWinLoop(Map<Integer, Long> costCount,List<Integer> costs,boolean findPair){
        if(costCount.isEmpty()){
            // 刚好干完了，从这里开始返回
            return true;
        }
        Integer iterCost = costs.get(0);
        Integer iterCost1 = iterCost + 1;
        Integer iterCost2 = iterCost + 2;
        Long iterCount = costCount.get(iterCost);
        Long iterCount1 = costCount.get(iterCost1);
        Long iterCount2 = costCount.get(iterCost2);

        // 找刻子
        if(iterCount >= 3){
            Map<Integer, Long> newCostCount = new HashMap<>(costCount);
            List<Integer> newCosts = new ArrayList<>(costs);
            if(iterCount==3){// 刚好3个，删掉
                newCostCount.remove(iterCost);
                newCosts.remove(iterCost);
            } else {
                newCostCount.put(iterCost,iterCount-3);
            }
            // 深度遍历，最后成功就返回
            if(checkWinLoop(newCostCount,newCosts,findPair)) return true;
        }
        // 找顺子
        if(iterCount1!=null && iterCount2!=null){
            Map<Integer, Long> newCostCount = new HashMap<>(costCount);
            List<Integer> newCosts = new ArrayList<>(costs);
            if(iterCount==1){// 刚好1个，删掉
                newCostCount.remove(iterCost);
                newCosts.remove(iterCost);
            } else {
                newCostCount.put(iterCost,iterCount-1);
            }
            if(iterCount1==1){// 刚好1个，删掉
                newCostCount.remove(iterCost1);
                newCosts.remove(iterCost1);
            } else {
                newCostCount.put(iterCost1,iterCount1-1);
            }
            if(iterCount2==1){// 刚好1个，删掉
                newCostCount.remove(iterCost2);
                newCosts.remove(iterCost2);
            } else {
                newCostCount.put(iterCost2,iterCount2-1);
            }
            // 深度遍历，最后成功就返回
            if(checkWinLoop(newCostCount,newCosts,findPair)) return true;
        }

        // 找搭子
        if (!findPair && iterCount >= 2) {
            Map<Integer, Long> newCostCount = new HashMap<>(costCount);
            List<Integer> newCosts = new ArrayList<>(costs);
            if(iterCount==2){// 刚好2个，删掉
                newCostCount.remove(iterCost);
                newCosts.remove(iterCost);
            } else {
                newCostCount.put(iterCost,iterCount-2);
            }
            // 深度遍历，最后成功就返回
            if(checkWinLoop(newCostCount,newCosts,true)) return true;
        }

        return false;
    }
}
