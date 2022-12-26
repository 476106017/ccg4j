package org.example.game;

import lombok.Data;
import org.example.card.FollowCard;

@Data
public class Damage{
    GameObj from;
    GameObj to;
    int damage;
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
        this.isFromAtk = true;
    }

    public boolean checkFollowAtArea(){
        if(getFrom() instanceof FollowCard fromFollow && !fromFollow.atArea())
            return false;
        if(getTo() instanceof FollowCard toFollow && !toFollow.atArea())
            return false;
        return true;
    }

    public GameObj another(GameObj obj){
        return from==obj ? to:from;
    }
}
