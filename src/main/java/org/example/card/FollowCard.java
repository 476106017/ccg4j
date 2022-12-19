package org.example.card;

import org.example.constant.CardType;
import org.example.game.GameObj;

import java.util.List;

public abstract class FollowCard extends Card{
    public final CardType TYPE = CardType.FOLLOW;
    public int atk = 0;
    public int hp = 0;
    public int maxHp = 0;
    public void entering(){}

    public void fanfare(List<GameObj> targets){
        info.msg(getName() + "发动战吼！");
    }
    public void deathrattle(){}

    @Override
    public void play(List<GameObj> targets) {
        info.msg(ownerPlayer().getName() + "使用了" + getName());
        super.play(targets);
        fanfare(targets);
    }

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
