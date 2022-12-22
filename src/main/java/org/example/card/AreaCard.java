package org.example.card;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.game.GameObj;

import java.lang.reflect.Modifier;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AreaCard  extends Card{
    public abstract String getType();
    public void entering(){}
    public void leaving(){}

    public void fanfare(List<GameObj> targets){}
    public void deathrattle(){}

    public void death(){
        info.msg(getNameWithOwner()+"被送入墓地！");
        ownerPlayer().getDeck().forEach(Card::charge);
        ownerPlayer().getGraveyard().add(this);
        ownerPlayer().countToGraveyard(1);
        ownerPlayer().getArea().remove(this);
        leaving();
        deathrattle();
    }

    @Override
    public void play(List<GameObj> targets) {
        super.play(targets);

        ownerPlayer().summon(this);
        fanfare(targets);

        ownerPlayer().getHand().remove(this);
    }


    public void effectBegin(){}

    public void effectEnd(){}
}
