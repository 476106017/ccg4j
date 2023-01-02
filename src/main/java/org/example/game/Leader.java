package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.constant.EffectTiming;
import org.example.system.function.FunctionN;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;


@Getter
@Setter
public abstract class Leader extends GameObj {


    private boolean needTarget = true;
    private boolean canUseSkill = true;


    private String overDrawMark = """
        输掉游戏
        """;
    // 默认超抽效果（输掉游戏）
    private Consumer<Integer> overDraw = integer -> info.gameset(enemyPlayer());

    public abstract String getJob();
    public abstract String getSkillName();
    public abstract String getSkillMark();
    public abstract int getSkillCost();
    public String getNameWithOwner(){
        return ownerPlayer().getName()+"的主战者【"+getName()+"】";
    };

    private List<Effect> effects = new ArrayList<>();

    public List<GameObj> targetable(){return new ArrayList<>();}

    public void skill(GameObj target){
        GameInfo info = ownerPlayer().getInfo();
        UUID me = ownerPlayer().getUuid();

        if(!isCanUseSkill()){
            info.msgToThisPlayer("现在无法使用主战者技能！");
            throw new RuntimeException();
        }
        if(getSkillCost() > ownerPlayer().getPpNum()){
            info.msgToThisPlayer("没有足够的pp以使用主战者技能！");
            throw new RuntimeException();
        }
        if(target!=null && !targetable().contains(target)){
            info.msgToThisPlayer("无法指定该目标！");
            throw new RuntimeException();
        }
        info.msg(ownerPlayer().getName() + "使用了"+getName()+"的主战者技能："+getSkillName());
        setCanUseSkill(false);
    };


    public void damaged(GameObj from,int damage){
        damaged(new Damage(from,this,damage));
    }
    public void damaged(Damage damage){
        GameInfo info = ownerPlayer().getInfo();

        useEffectWithDamage(EffectTiming.LeaderDamaged,damage);

        ownerPlayer().setHp(ownerPlayer().getHp()- damage.getDamage());
        info.msg(getNameWithOwner()+"受到了来自"+damage.getFrom().getName()+"的"+damage.getDamage()+"点伤害！" +
            "（剩余"+ownerPlayer().getHp()+"点生命值）");
        if (ownerPlayer().getHp() <= 0) {
            info.gameset(ownerPlayer().getEnemy());// 敌方获胜
        }
    }

    public void addEffect(Card source, EffectTiming timing, FunctionN effect){
        addEffect(source,timing,damage -> effect.apply());
    }

    public void addEffect(Card source, EffectTiming timing, Consumer<Damage> effect){
        addEffect(source,timing,-1,true,effect);
    }

    public void addEffect(Card source, EffectTiming timing,int canUseTurn,boolean only, Consumer<Damage> effect){
        if(only && effects.stream()
            .anyMatch(e -> e.getSource().getClass().equals(source.getClass()))){
            info.msg("该主战者效果不能叠加！");
            return;
        }
        info.msg(source.getNameWithOwner() + "为" + ownerPlayer().getName() + "提供了主战者效果！");
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
                    ownerPlayer().getInfo().msg(effect.getSource().getNameWithOwner() + "提供的主战者效果已消失");
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

    @Getter
    @Setter
    public static class Effect{
        private EffectTiming timing;
        private int canUseTurn;// 可使用回合（包含敌方回合）

        private Card source;

        private Consumer<Damage> effect;

        public Effect(Card source, EffectTiming timing,int canUseTurn,Consumer<Damage> effect) {
            this.source = source;
            this.timing = timing;
            this.canUseTurn = canUseTurn;
            this.effect = effect;
        }

    }

}
