package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;

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

        damage.apply();

        info.msg(getNameWithOwner()+"受到了来自"+damage.getFrom().getName()+"的"+damage.getDamage()+"点伤害！" +
            "（剩余"+ownerPlayer().getHp()+"点生命值）");
    }


    public void addEffect(Effect newEffect,boolean only){
        newEffect.setOwnerObj(this);
        if(only && effects.stream()
            .anyMatch(e ->
                // 相同创建者、和相同效果时机，不能叠加
                e.getParent().getClass().equals(newEffect.getParent().getClass())
                    && e.getTiming().equals(newEffect.getTiming())
            )){
            info.msg("该主战者效果不能叠加！");
            return;
        }
        info.msg(newEffect.getParent().getNameWithOwner() + "为" + ownerPlayer().getName() + "提供了主战者效果！");
        effects.add(newEffect);
    }

    public List<Effect> getEffectsWhen(EffectTiming timing){
        return getEffects().stream()
            .filter(effect -> timing.equals(effect.getTiming()))
            .toList();

    }
    public void expireEffect(){
        // 过期主战者效果
        List<Effect> usedUpEffects = new ArrayList<>();
        getEffects()
            .forEach(effect -> {
                int canUse = effect.getCanUseTurn();
                if(canUse == 1){
                    // 用完了销毁
                    usedUpEffects.add(effect);
                    ownerPlayer().getInfo().msg(effect.getParent().getNameWithOwner() + "提供的主战者效果已消失");
                }else if (canUse > 1){
                    effect.setCanUseTurn(canUse-1);
                }
            });
        getEffects().removeAll(usedUpEffects);
    }
}
