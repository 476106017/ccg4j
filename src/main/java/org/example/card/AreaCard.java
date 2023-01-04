package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.game.GameObj;
import org.example.system.function.FunctionN;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


@Getter
@Setter
public abstract class AreaCard extends Card{
    public abstract String getType();

    public void backToHand(){
        if(!atArea())return;

        info.msg(getNameWithOwner() + "返回手牌！");


        removeWhenAtArea();
        tempEffects(EffectTiming.Leaving);


        if (this instanceof FollowCard followCard){
            // 随从回去，装备破坏
            if (followCard.equipped()) {
                info.msg(getNameWithOwner() + "的装备被留在了战场！");
                followCard.getEquipment().death();
            }
            followCard.setHp(followCard.getMaxHp());// 回复到满血
        }else if (this instanceof AmuletCard amuletCard){
            amuletCard.setCountDown(((AmuletCard)(amuletCard.prototype())).getCountDown());
        }

        ownerPlayer().addHand(this);

        tempEffects(EffectTiming.WhenBackToHand);
    }

    public boolean destroyedBy(GameObj from){
        if(!atArea())return false;

        if(hasKeyword("无法破坏")) {
            info.msg(getNameWithOwner() + "无法破坏！");
            return false;
        }
        info.msg(getNameWithOwner() + "被"+from.getNameWithOwner()+"破坏！");
        death();
        if(this instanceof FollowCard thisFollow && from instanceof Card card){
            card.tempEffects(EffectTiming.WhenKill,thisFollow);
        }
        return true;
    }

    public void death(){
        if(!atArea())return;
        info.msg(getNameWithOwner()+"被送入墓地！");

        if(hasKeyword("游魂")){
            info.msg("墓地拒绝了【游魂】！");
            info.exile(this);
            return;
        }

        // 注能
        ownerPlayer().getHand().forEach(card ->card.useEffects(EffectTiming.Charge,this));

        removeWhenAtArea();
        tempEffects(EffectTiming.Leaving);
        tempEffects(EffectTiming.DeathRattle);

        if(this instanceof FollowCard followCard && followCard.equipped()){
            followCard.getEquipment().death();
        }

        ownerPlayer().getGraveyard().add(this);
        ownerPlayer().countToGraveyard(1);
    }

    public static class Event {
        /** 回合开始效果 */
        public record EffectBegin(FunctionN effect){}
        /** 回合结束效果 */
        public record EffectEnd(FunctionN effect){}
        /** 入场时（不含变身/控制） */
        public record Entering(FunctionN effect){}
        /** 离场时（不含变身/控制） */
        public record Leaving(FunctionN effect){}
        /** 离场时（不含变身/控制） */
        public record WhenBackToHand(FunctionN effect){}
        /** 当卡牌在场 */
        public record WhenAtArea(FunctionN effect){}
        /** 当卡牌不在场 */
        public record WhenNoLongerAtArea(FunctionN effect){}
        /** 亡语 */
        public record DeathRattle(FunctionN effect){}
        /** 召唤卡牌时 */
        public record WhenSummon(Consumer<AreaCard> effect){}
        /** 对手召唤卡牌时 */
        public record WhenEnemySummon(Consumer<AreaCard> effect){}
        /** 抽牌时 List<Card>是抽的牌，但是并不一定会留着手上（爆牌） */
        public record WhenDraw(Consumer<List<Card>> effect){
            public WhenDraw(FunctionN effect) {
                this(cards -> effect.apply());
            }
        }
        /** 对手抽牌时 */
        public record WhenEnemyDraw(Consumer<List<Card>> effect){
            public WhenEnemyDraw(FunctionN effect) {
                this(cards -> effect.apply());
            }
        }
    }
}
