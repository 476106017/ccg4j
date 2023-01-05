package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.EquipmentCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.system.function.FunctionN;
import org.example.system.function.PredicateN;

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
            try {
                Thread.sleep(10);
            }catch (Exception ignored){}
            if(effect.getCanEffect().test(param)){
                GameObj effectOwnerCard = effect.getOwnerObj();
                PlayerInfo ownerPlayer = effectOwnerCard.ownerPlayer();
                GameInfo info = effectOwnerCard.getInfo();
                if(effect.getTiming().isSecret())
                    info.msgTo(ownerPlayer.getUuid(),effectOwnerCard.getIdWithOwner() + "发动【"+ effect.getTiming().getName() +"】效果");
                else
                    info.msg(effectOwnerCard.getIdWithOwner() + "发动【"+ effect.getTiming().getName() +"】效果");
                // region 瞬召卡片要在发动效果前召唤/揭示
                if(effect.getTiming().equals(EffectTiming.InvocationBegin) || effect.getTiming().equals(EffectTiming.InvocationEnd)){
                    if(effectOwnerCard instanceof AreaCard areaCard){
                        areaCard.ownerPlayer().getDeck().remove(areaCard);
                        // 装备卡自己写逻辑（装备给谁）
                        if(!(areaCard instanceof EquipmentCard))
                            areaCard.ownerPlayer().summon(areaCard);
                    }
                    if(effectOwnerCard instanceof SpellCard spellCard){
                        spellCard.ownerPlayer().getHand().add(spellCard);
                        spellCard.ownerPlayer().getDeck().remove(spellCard);
                    }
                }
                // endregion 瞬召卡片要在发动效果前召唤/揭示
                // region 场上击杀时要判断结算时是否在场
                if(effect.getTiming().equals(EffectTiming.WhenKill)
                    && effect.getOwnerObj() instanceof AreaCard areaCard
                    && !areaCard.atArea()) return;
                // endregion 击杀时要判断结算时是否在场
                effect.getEffect().accept(param);
            }
        }

    }
}