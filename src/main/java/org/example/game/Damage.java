package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;

import java.util.List;

@Getter
@Setter
public class Damage{
    GameObj from;
    GameObj to;
    int damage;
    boolean isCounter = false;
    boolean isFromAtk = false;

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

    public void reduce(){
        if(to instanceof FollowCard toFollow && toFollow.atArea()) {
            if (!isFromAtk && toFollow.hasKeyword("效果伤害免疫")) {
                setDamage(0);
                toFollow.getInfo().msg(toFollow.getNameWithOwner() + "免疫了效果伤害！");
            } else {
                int reduce = 0;
                reduce += toFollow.countKeyword("伤害减免");
                if (isFromAtk())
                    reduce += toFollow.countKeyword("护甲");
                else
                    reduce += toFollow.countKeyword("魔抗");

                if (reduce > 0) {
                    int finalReduce = Math.min(getDamage(), reduce);
                    toFollow.getInfo().msg(toFollow.getNameWithOwner() + "减少了" + finalReduce + "点伤害");
                    setDamage(getDamage() - finalReduce);
                }
            }
        }
    }


    public void apply(){
        new DamageMulti(to.getInfo(), List.of(this)).apply();
    }
}
