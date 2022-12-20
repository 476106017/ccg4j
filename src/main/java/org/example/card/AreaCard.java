package org.example.card;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.constant.CardType;
import org.example.game.GameObj;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AreaCard  extends Card{
    public void entering(){}

    public void fanfare(List<GameObj> targets){
        info.msg(getName() + "发动战吼！");
    }
    public void deathrattle(){}

    public void death(){
        info.msg((ownerPlayer().getName())+"的"+getName()+"因被破坏而送入墓地！");
        deathrattle();
        ownerPlayer().getGraveyard().add(this);
        ownerPlayer().countToGraveyard(1);
        ownerPlayer().getArea().remove(this);
    }

    @Override
    public void play(List<GameObj> targets) {
        super.play(targets);
        info.msg(ownerPlayer().getName() + "使用了" + getName());
        entering();
        fanfare(targets);

        ownerPlayer().getArea().add(this);
        ownerPlayer().getHand().remove(this);
    }

    /**
     * 回合结束的瞬召
     */
    public boolean canInstantBegin() {
        return false;
    }

    /**
     * 回合开始的瞬召
     */
    public boolean canInstantEnd() {
        return false;
    }

    public void afterInstantBegin(){}

    public void afterInstantEnd(){}

    /**
     * 回合结束的效果
     */
    public boolean canEffectBegin() {
        return false;
    }

    /**
     * 回合开始的效果
     */
    public boolean canEffectEnd() {
        return false;
    }

    public void effectBegin(){}

    public void effectEnd(){}
}
