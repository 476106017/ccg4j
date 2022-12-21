package org.example.game;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.constant.EffectTiming;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@EqualsAndHashCode(callSuper = true)
@Data
public class Leader extends GameObj {

    private PlayerInfo playerInfo;

    private String job;

    private List<Effect> effects;

    public Leader(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    public void addEffect(Effect effect){
        effects.add(effect);
    }

    @Data
    public static class Effect{
        private EffectTiming timing;
        private int canUse;

        private Card source;

        private Consumer<PlayerInfo> effect;

        public Effect(Card source, EffectTiming timing,int canUse, Consumer<PlayerInfo> effect) {
            this.source = source;
            this.timing = timing;
            this.canUse = canUse;
            this.effect = effect;
        }

    }

}
