package org.example.card;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.constant.CardType;
import org.example.constant.Patten;
import org.example.game.GameInfo;

public abstract class FollowCard extends Card{
    public final CardType TYPE = CardType.FOLLOW;
    public int atk = 0;
    public int hp = 0;
    public int maxHp = 0;
    public void entering(){};
    public void fanfare(){}
    public void deathrattle(){};

    @Override
    public String getType() {
        return TYPE.getName();
    }

    public boolean damagedDeath(int damage){
        if(hp>damage){
            hp -= damage;
            return false;
        }else {
            hp = 0;
            return true;
        }
    }
}
