package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.List;

import static org.example.constant.CounterKey.BLOCK;
import static org.example.constant.CounterKey.STRENGTH;

@Getter
@Setter
public class Damage{
    GameObj from;
    GameObj to;
    int damage;
    boolean isCounter = false;
    boolean isFromAtk = false;
    boolean miss = false;

    public Damage(GameObj from, GameObj to, int damage) {
        this.from = from;
        this.to = to;
        this.damage = damage;
    }
    public Damage(GameObj from, GameObj to) {
        assert from instanceof FollowCard;
        this.from = from;
        this.to = to;
        this.damage = ((FollowCard) from).getAtk();
        this.isFromAtk = true;
    }

    public void addDamage(int add){
        damage = Math.max(0,damage+add);
    }

    public GameObj another(GameObj obj){
        if(getFrom() == obj)
            return getTo();
        return getFrom();
    }

    public boolean checkFollowAtArea(){
        if(getFrom() instanceof FollowCard fromFollow && !fromFollow.atArea())
            return false;
        if(getTo() instanceof FollowCard toFollow && !toFollow.atArea())
            return false;
        return true;
    }

    public boolean avoid(){
        if (to instanceof FollowCard toFollow && toFollow.atArea()) {
            if (toFollow.hasKeyword("圣盾")) {
                toFollow.getInfo().msg(toFollow.getNameWithOwner() + "的圣盾抵消了本次伤害！");
                toFollow.removeKeyword("圣盾");
                return true;
            }
            if (isFromAtk()) {
                if (isMiss()) {
                    toFollow.getInfo().msg(toFollow.getNameWithOwner() + "闪避了本次攻击伤害！");
                    return true;
                }
            }
            if (!isFromAtk()) {
                if (toFollow.hasKeyword("魔法免疫")) {
                    toFollow.getInfo().msg(toFollow.getNameWithOwner() + "免疫了本次效果伤害！");
                    return true;
                }
                if (toFollow.hasKeyword("魔法护盾")) {
                    toFollow.getInfo().msg(toFollow.getNameWithOwner() + "的魔法护盾抵消了本次效果伤害！");
                    toFollow.removeKeyword("魔法护盾");
                    return true;
                }
            }
        }
        return false;
    }

    public void reduce(){
        Integer strength = 0;
        try{
            strength = from.ownerPlayer().getCount(STRENGTH);
        }catch (Exception ignored){}
        if(strength>0){
            to.getInfo().msg(from.ownerLeader().getNameWithOwner() + "的力量使本次伤害增加" + strength);
            addDamage(strength);
        }
        if(to instanceof FollowCard toFollow && toFollow.atArea()) {
            if (!isFromAtk && toFollow.hasKeyword("效果伤害免疫")) {
                setDamage(0);
                toFollow.getInfo().msg(toFollow.getNameWithOwner() + "免疫了效果伤害！");
            } else if(!(from instanceof Card card && card.hasKeyword("穿透"))) {
                // 没有穿透效果，计算减免
                int reduce = 0;
                reduce += toFollow.countKeyword("伤害减免");
                if (isFromAtk())
                    reduce += toFollow.countKeyword("护甲");
                else
                    reduce += toFollow.countKeyword("魔抗");

                if (reduce > 0) {
                    int finalReduce = Math.min(getDamage(), reduce);
                    setDamage(getDamage() - finalReduce);
                    toFollow.getInfo().msg(toFollow.getNameWithOwner() + "通过抗性减少了" + finalReduce + "点伤害");
                }

                int parry = toFollow.countKeyword(BLOCK);
                if(getDamage()>0 && parry>0){
                    int parryReduce = Math.min(getDamage(), parry);
                    setDamage(getDamage() - parryReduce);
                    toFollow.removeKeyword(BLOCK,parryReduce);
                    toFollow.getInfo().msg(toFollow.getNameWithOwner() + "格挡了" + parryReduce + "点伤害（还剩"+toFollow.countKeyword("格挡")+"点格挡）");
                }
            }
        } else if (to instanceof Leader leader && !(from instanceof Card card && card.hasKeyword("穿透"))) {
            final PlayerInfo playerInfo = to.ownerPlayer();
            // 没有穿透效果，计算减免
            int reduce = 0;
            reduce += playerInfo.getCount("伤害减免");
            if (isFromAtk())
                reduce += playerInfo.getCount("护甲");
            else
                reduce += playerInfo.getCount("魔抗");

            if (reduce > 0) {
                int finalReduce = Math.min(getDamage(), reduce);
                setDamage(getDamage() - finalReduce);
                leader.getInfo().msg(leader.getNameWithOwner() + "通过抗性减少了" + finalReduce + "点伤害");
            }

            int parry = playerInfo.getCount(BLOCK);
            if(getDamage()>0 && parry>0){
                int parryReduce = Math.min(getDamage(), parry);
                setDamage(getDamage() - parryReduce);
                playerInfo.count("格挡",-parryReduce);
                leader.getInfo().msg(leader.getNameWithOwner() + "格挡了" + parryReduce + "点伤害（还剩"+playerInfo.getCount("格挡")+"点格挡）");
            }
        }
    }


    public void apply(){
        new DamageMulti(to.getInfo(), List.of(this)).apply();
    }
}
