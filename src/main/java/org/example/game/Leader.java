package org.example.game;

import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


@Getter
@Setter
public abstract class Leader extends GameObj {


    private transient boolean needTarget = true;
    private transient boolean canUseSkill = true;
    private transient String Mark = "";// 能力介绍

    private transient String overDrawMark = """
        输掉游戏
        """;
    // 默认超抽效果（输掉游戏）
    private transient Consumer<Integer> overDraw = integer -> info.gameset(enemyPlayer());

    private transient List<Effect> effects = new ArrayList<>();

    public abstract String getJob();
    public abstract String getSkillName();
    public abstract String getSkillMark();
    public abstract int getSkillCost();
    public String getNameWithOwner(){
        return ownerPlayer().getName()+"的主战者【"+getName()+"】";
    };
    public Integer getHp(){
        return ownerPlayer().getHp();
    };


    public List<GameObj> targetable(){return new ArrayList<>();}

    public void skill(GameObj target){
        GameInfo info = ownerPlayer().getInfo();
        Session me = ownerPlayer().getSession();

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
        ownerPlayer().setPpNum(ownerPlayer().getPpNum() - getSkillCost());
    };


    public void damaged(GameObj from,int damage){
        new Damage(from,this,damage).apply();
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
        info.msg(newEffect.getParent().getNameWithOwner() + "为" + ownerPlayer().getName() + "提供了"+newEffect.getTiming().getName()+"效果！");
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
