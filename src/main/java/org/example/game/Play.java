package org.example.game;

import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.system.function.FunctionN;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** 使用（到场上叫做战吼，法术牌释放效果） */
public record Play(Supplier<List<List<GameObj>>> canTargets, int targetNum, boolean mustTarget,
                   int choiceNum, BiConsumer<Integer,List<GameObj>> effect){
    /**
     * 仅抉择
     */
    public Play(int choiceNum, Consumer<Integer> effect) {
        this(ArrayList::new,0, false,
            choiceNum,((integer, gameObjs) -> effect.accept(integer)));
    }
    /**
     * 仅选择目标
     */
    public Play(Supplier<List<List<GameObj>>> targets, int targetNum,boolean mustTarget,Consumer<List<GameObj>> effect) {
        this(targets,targetNum,mustTarget,
            0,((integer, gameObjs) -> effect.accept(gameObjs)));
    }
    /**
     * 仅选择一个目标
     */
    public Play(Supplier<List<GameObj>> targets, boolean mustTarget,Consumer<GameObj> effect) {
        this(()->List.of(targets.get()) ,1,mustTarget,
            0,((integer, gameObjs) -> effect.accept(gameObjs.get(0))));
    }
    /**
     * 必须一个目标，抉择
     */
    public Play(Supplier<List<GameObj>> targets,
                int choiceNum, BiConsumer<Integer,GameObj> effect) {
        this(()->List.of(targets.get()) ,1,true,choiceNum,
            ((integer, gameObjs) -> effect.accept(integer,gameObjs.get(0))));
    }
    /**
     * 须选择一个目标，没有任何效果（装备牌）
     */
    public Play(Supplier<List<GameObj>> targets) {
        this(()->List.of(targets.get()) ,1,true,0,((integer, gameObjs) -> {}));
    }
    /**
     * 不需要抉择/选择目标
     */
    public Play(FunctionN effect) {
        this(ArrayList::new,0, false,
            0,((integer, gameObjs) -> effect.apply()));
    }

    public String describeCanTargets(){
        List<List<GameObj>> canTargetsL = canTargets.get();
        if(canTargetsL.isEmpty()) return "无可指定目标！";

        StringBuilder sb = new StringBuilder("【可指定的目标】\n");
        for (int i = 0; i < canTargetsL.size(); i++) {
            List<GameObj> gameObjs = canTargetsL.get(i);
            sb.append("目标【").append(i+1).append("】\n");
            for (int j = 0; j < gameObjs.size(); j++) {
                GameObj gameObj = gameObjs.get(j);
                sb.append("\t【").append(j+1).append("】\t");

                if(gameObj instanceof Leader leader) {
                    sb.append(leader.getNameWithOwner());
                } else if (gameObj instanceof Card targetCard){
                    sb.append(targetCard.getNameWithOwnerWithPlace()).append("\t");
                    if(gameObj instanceof FollowCard follow){
                        sb.append(follow.getAtk()).append("/").append(follow.getHp())
                            .append("\t").append(follow.getMaxHp()==follow.getHp()?"满":"残").append("\t");
                        if(follow.getEquipment()!=null) {
                            sb.append("装备中：").append(follow.getEquipment().getName());
                            if (follow.getEquipment().getCountdown() != -1)
                                sb.append("（").append(follow.getEquipment().getCountdown()).append("）");
                            sb.append("\t");
                        }
                    }else if(gameObj instanceof AmuletCard amuletCard){
                        sb.append("护符\t").append("倒数：").append(amuletCard.getCountDown());
                    }
                }
            }
            sb.append("\n");
        }
        return sb.toString();

    }

}