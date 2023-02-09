package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.EquipmentCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.system.function.FunctionN;
import org.example.system.function.PredicateN;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
@Setter
public class Effect{
    private GameObj parent;
    private GameObj ownerObj;
    private EffectTiming timing;
    private int canUseTurn = -1; // 可使用回合（包含敌方回合）
    private Predicate<Object> canEffect = o -> true;
    private Consumer<Object> effect;

    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, int canUseTurn,
                  Predicate<Object> canEffect, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.canUseTurn = canUseTurn;
        this.canEffect = canEffect;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing,
                  Predicate<Object> canEffect, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.canEffect = canEffect;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing,
                  PredicateN canEffect, FunctionN effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.canEffect = obj-> canEffect.test();
        this.effect = obj-> effect.apply();
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, int canUseTurn, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.canUseTurn = canUseTurn;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, int canUseTurn, FunctionN effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.canUseTurn = canUseTurn;
        this.effect = obj->effect.apply();
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, FunctionN effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.effect = obj->effect.apply();
    }

    public PlayerInfo ownerPlayer(){
        return ownerObj.ownerPlayer();
    }

    @Setter
    @Getter
    public static class EffectInstance {
        private Effect effect;
        private Object param;

        public EffectInstance(Effect effect, Object param) {
            assert param==null || effect.getTiming().getParamClass()==param.getClass();
            this.effect = effect;
            this.param = param;
        }

        EffectInstance(Effect effect) {
            this(effect, null);
        }

        public void consume() {
            if(effect.getCanEffect().test(param)){
                GameObj effectOwnerCard = effect.getOwnerObj();
                PlayerInfo ownerPlayer = effectOwnerCard.ownerPlayer();
                GameInfo info = effectOwnerCard.getInfo();

                // region 判断结算时是否在场
                if(List.of(EffectTiming.WhenKill,EffectTiming.WhenAtArea,
                        EffectTiming.WhenRecalled,EffectTiming.WhenOthersRecall,
                        EffectTiming.WhenOverDraw,EffectTiming.WhenEnemyOverDraw,
                        EffectTiming.Entering,EffectTiming.AfterDamaged,
                        EffectTiming.WhenDraw,EffectTiming.WhenEnemyDraw,
                        EffectTiming.WhenSummon,EffectTiming.WhenEnemySummon,
                        EffectTiming.WhenDestroy,EffectTiming.WhenEnemyDestroy,
                        EffectTiming.AfterLeaderDamaged,EffectTiming.LeaderHealed)
                        .contains(effect.getTiming())
                    && effect.getOwnerObj() instanceof AreaCard areaCard
                    && !areaCard.atArea()) return;
                // endregion 判断结算时是否在场
                // region 判断结算时是否离场
                if(List.of(EffectTiming.WhenNoLongerAtArea,EffectTiming.Leaving,
                        EffectTiming.Exile,EffectTiming.DeathRattle)
                    .contains(effect.getTiming())
                    && effect.getOwnerObj() instanceof Card card
                    && card.atArea()) return;
                // endregion 判断结算时是否离场
                // region 判断结算时是否在手牌
                if(List.of(EffectTiming.Charge,EffectTiming.WhenBackToHand)
                    .contains(effect.getTiming())
                    && effect.getOwnerObj() instanceof Card card
                    && !card.atHand()) return;
                // endregion 判断结算时是否在场

                if(effect.getTiming().isSecret())
                    info.msgTo(ownerPlayer.getUuid(),effectOwnerCard.getNameWithOwner() + "发动【"+ effect.getTiming().getName() +"】效果");
                else
                    info.msg(effectOwnerCard.getNameWithOwner() + "发动【"+ effect.getTiming().getName() +"】效果");
                // region 瞬召卡片要在发动效果前召唤/揭示
                if(effect.getTiming().equals(EffectTiming.InvocationBegin) || effect.getTiming().equals(EffectTiming.InvocationEnd)){
                    if(effectOwnerCard instanceof AreaCard areaCard &&
                        ownerPlayer.getArea().size() < ownerPlayer.getAreaMax()){// 有足够空间才召唤
                        ownerPlayer.getDeck().remove(areaCard);
                        // 装备卡自己写逻辑（装备给谁）
                        if(!(areaCard instanceof EquipmentCard))
                            ownerPlayer.summon(areaCard);
                    }
                    if(effectOwnerCard instanceof SpellCard spellCard){
                        ownerPlayer.getHand().add(spellCard);
                        ownerPlayer.getDeck().remove(spellCard);
                    }
                }
                // endregion 瞬召卡片要在发动效果前召唤/揭示
                effect.getEffect().accept(param);
            }
        }

    }
}