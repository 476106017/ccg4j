package org.example.game;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.constant.EffectTiming;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Leader extends GameObj {

    private PlayerInfo playerInfo;

    public abstract String getName();
    public abstract String getJob();
    public abstract String getSkillName();
    public abstract String getSkillMark();
    public abstract int getSkillCost();
    public boolean isCanUseSkill(){
        return true;
    }

    private List<Effect> effects = new ArrayList<>();


    public void skill(GameObj target){
        GameInfo info = playerInfo.getInfo();
        UUID me = playerInfo.getUuid();

        if(!isCanUseSkill()){
            info.msgTo(me,"现在无法使用主战者技能！");
            return;
        }
        if(getSkillCost() > getPlayerInfo().getPpNum()){
            info.msgTo(me,"没有足够的pp以使用主战者技能！");
            return;
        }
    };

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
