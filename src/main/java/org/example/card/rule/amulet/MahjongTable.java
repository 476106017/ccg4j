package org.example.card.rule.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.PlayerInfo;
import org.example.system.Lists;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.PLAY_NUM;

@Getter
@Setter
public class MahjongTable extends AmuletCard {

    public Integer cost = 7;

    public String name = "自动麻将桌";
    public String job = "棋牌规则";
    private List<String> race = Lists.ofStr("机器");
    public String mark = """
        双方抽牌时：
        如果手牌费用符合门清自摸和，则获得游戏胜利
        —————————————
        双方召唤时：
        如果场上还有空间，对方以召唤牌的费用为参考，从手牌进行鸣牌并召唤到场上
        """;
    public String subMark = "";


    public MahjongTable() {
        getWhenSummons().add(new Event.WhenSummon(areaCard -> checkSummon(enemyPlayer(),areaCard)));
        getWhenEnemySummons().add(new Event.WhenEnemySummon(areaCard -> checkSummon(ownerPlayer(),areaCard)));
        getWhenDraws().add(new Event.WhenDraw(() -> checkWin(ownerPlayer())));
        getWhenEnemyDraws().add(new Event.WhenEnemyDraw(() -> checkWin(ownerPlayer())));
    }

    private void checkSummon(PlayerInfo player, AreaCard areaCard){
        if(info.thisPlayer()==player) return;// 是本回合的玩家不需要检查
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

        if (isWin) info.gameset(player);
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
