package org.example.game;

import lombok.Getter;
import lombok.Setter;
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
    private Class timingParamClass;
    private int canUseTurn = -1; // 可使用回合（包含敌方回合）
    private Predicate<Object> canEffect;
    private Consumer<Object> effect;

    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, int canUseTurn,
                  Predicate<Object> canEffect, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.timingParamClass = timing.getParamClass();
        this.canUseTurn = canUseTurn;
        this.canEffect = canEffect;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing,
                  Predicate<Object> canEffect, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.timingParamClass = timing.getParamClass();
        this.canEffect = canEffect;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing,
                  PredicateN canEffect, FunctionN effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.timingParamClass = timing.getParamClass();
        this.canEffect = obj-> canEffect.test();
        this.effect = obj-> effect.apply();
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, int canUseTurn, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.timingParamClass = timing.getParamClass();
        this.canUseTurn = canUseTurn;
        this.canEffect = obj-> true;
        this.effect = effect;
    }
    public Effect(GameObj parent, GameObj ownerObj, EffectTiming timing, Consumer<Object> effect) {
        this.parent = parent;
        this.ownerObj = ownerObj;
        this.timing = timing;
        this.timingParamClass = timing.getParamClass();
        this.canEffect = obj-> true;
        this.effect = effect;
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
                if(effect.getTiming().isSecret())
                    info.msgTo(ownerPlayer.getUuid(),effectOwnerCard.getName()+ "发动【"+ effect.getTiming().getName() +"】效果");
                else
                    info.msg(effectOwnerCard.getNameWithOwner()+ "发动【"+ effect.getTiming().getName() +"】效果");
                effect.getEffect().accept(param);
            }
        }

    }
}