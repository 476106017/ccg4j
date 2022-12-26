package org.example.game;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.example.constant.CounterKey.*;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Leader extends GameObj {

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

        // 护盾效果
        Integer shield = getPlayerInfo().getCount(DAMAGE_SHIELD);
        Integer atkShield = getPlayerInfo().getCount(ATK_SHIELD);
        Integer effectShield = getPlayerInfo().getCount(EFFECT_SHIELD);
        if(damage.isFromAtk()){
            shield += atkShield;
        }else {
            shield += effectShield;
        }
        if(shield>0){
            damage.setDamage(// 护盾减免伤害，但受伤不能小于0
                Math.max(damage.getDamage()-shield, 0));
            info.msg(getNameWithOwner() + "通过护盾减免了"+shield+"点伤害");
        }


        // TODO 主战者受伤效果
//        if(!getWhenDamageds().isEmpty()){
//            info.msg(getNameWithOwner() + "发动受伤时效果！");
//        }
//        getWhenDamageds().forEach(whenDamaged -> whenDamaged.effect().accept(damage));

        getPlayerInfo().setHp(getPlayerInfo().getHp()- damage.getDamage());
        info.msg(getNameWithOwner()+"受到了来自"+damage.getFrom().getName()+"的"+damage.getDamage()+"点伤害！" +
            "（剩余"+getPlayerInfo().getHp()+"点生命值）");
        if (getPlayerInfo().getHp() <= 0) {
            info.gameset(getPlayerInfo().getEnemy());// 敌方获胜
        }
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
