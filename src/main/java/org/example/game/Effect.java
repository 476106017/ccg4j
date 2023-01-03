package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;

import java.util.function.Consumer;

@Getter
@Setter
public class Effect{
    private GameObj source;
    private GameObj owner;
    private EffectTiming timing;
    private int canUseTurn; // 可使用回合（包含敌方回合）
    private Consumer<Damage> effect;

    public Effect(GameObj source, GameObj owner, EffectTiming timing, int canUseTurn, Consumer<Damage> effect) {
        this.source = source;
        this.owner = owner;
        this.timing = timing;
        this.canUseTurn = canUseTurn;
        this.effect = effect;
    }

    public record EffectInstance(Effect effect,Damage damage){
        EffectInstance(Effect effect){
            this(effect,null);
        }
    }
}