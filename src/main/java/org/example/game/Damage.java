package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;

@Getter
@Setter
public class Damage{
    GameObj from;
    GameObj to;
    int damage;
    int countDamage = 0;
    boolean isFromAtk;

    public Damage(GameObj from, GameObj to, int damage) {
        this.from = from;
        this.to = to;
        this.damage = damage;
        this.isFromAtk = false;
    }
    public Damage(GameObj from, GameObj to) {
        assert from instanceof FollowCard;
        this.from = from;
        this.to = to;
        this.damage = ((FollowCard) from).getAtk();
        if (to instanceof FollowCard toFollow){
            this.countDamage = toFollow.getAtk();
        }
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

}
