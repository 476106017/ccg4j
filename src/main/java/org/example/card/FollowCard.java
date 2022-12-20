package org.example.card;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.constant.CardType;
import org.example.game.GameObj;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
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
        super.play(targets);
        info.msg(ownerPlayer().getName() + "使用了" + getName());
        fanfare(targets);

        ownerPlayer().getArea().add(this);
        ownerPlayer().getHand().remove(this);
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
