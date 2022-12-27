package org.example.game;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.constant.EffectTiming;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Leader extends GameObj {

    @EqualsAndHashCode.Exclude
    private PlayerInfo playerInfo;

    private boolean canUseSkill = true;

    public abstract String getJob();
    public abstract String getSkillName();
    public abstract String getSkillMark();
    public abstract int getSkillCost();
    public String getNameWithOwner(){
        return getPlayerInfo().getName()+"的主战者【"+getName()+"】";
    };

    private List<Effect> effects = new ArrayList<>();

    public List<GameObj> targetable(){return new ArrayList<>();}

    public void skill(GameObj target){
        GameInfo info = playerInfo.getInfo();
        UUID me = playerInfo.getUuid();

        if(!isCanUseSkill()){
            info.msgTo(me,"现在无法使用主战者技能！");
            throw new RuntimeException();
        }
        if(getSkillCost() > getPlayerInfo().getPpNum()){
            info.msgTo(me,"没有足够的pp以使用主战者技能！");
            throw new RuntimeException();
        }
        if(target!=null && !targetable().contains(target)){
            info.msgTo(me,"无法指定该目标！");
            throw new RuntimeException();
        }
        info.msg(getPlayerInfo().getName() + "使用了"+getName()+"的主战者技能："+getSkillName());
    };

    public void damaged(Damage damage){
        GameInfo info = getPlayerInfo().getInfo();

        useEffectWithDamage(EffectTiming.LeaderDamaged,damage);

        getPlayerInfo().setHp(getPlayerInfo().getHp()- damage.getDamage());
        info.msg(getNameWithOwner()+"受到了来自"+damage.getFrom().getName()+"的"+damage.getDamage()+"点伤害！" +
            "（剩余"+getPlayerInfo().getHp()+"点生命值）");
        if (getPlayerInfo().getHp() <= 0) {
            info.gameset(getPlayerInfo().getEnemy());// 敌方获胜
        }
    }

    public void addEffect(Card source, EffectTiming timing,int canUseTurn, Consumer<Damage> effect){
        effects.add(new Effect(source,timing,canUseTurn,effect));
    }

    public List<Effect> getEffectsWhen(EffectTiming timing){
        return getEffects().stream()
            .filter(effect -> timing.equals(effect.getTiming()))
            .toList();

    }
    public void expireEffect(){
        // 过期主战者效果
        List<Leader.Effect> usedUpEffects = new ArrayList<>();
        getEffects()
            .forEach(effect -> {
                int canUse = effect.getCanUseTurn();
                if(canUse == 1){
                    // 用完了销毁
                    usedUpEffects.add(effect);
                    getPlayerInfo().getInfo().msg(effect.getSource().getNameWithOwner() + "提供的主战者效果已消失");
                }else if (canUse > 1){
                    effect.setCanUseTurn(canUse-1);
                }
            });
        getEffects().removeAll(usedUpEffects);
    }

    public void useEffect(EffectTiming timing){
        useEffectWithDamage(timing,null);
    }
    public void useEffectWithDamage(EffectTiming timing,Damage damage){
        getEffectsWhen(timing).forEach(effect -> effect.getEffect().accept(damage));
    }

    @Data
    public static class Effect{
        private EffectTiming timing;
        private int canUseTurn;// 可使用回合（包含对方回合）

        private Card source;

        private Consumer<Damage> effect;

        public Effect(Card source, EffectTiming timing,int canUseTurn, Consumer<Damage> effect) {
            this.source = source;
            this.timing = timing;
            this.canUseTurn = canUseTurn;
            this.effect = effect;
        }

    }

}
