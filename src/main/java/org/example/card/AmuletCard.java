package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardType;


@Getter
@Setter
public abstract class AmuletCard extends AreaCard{

    public final CardType TYPE = CardType.AMULET;
    public transient int countDown = -1;

    @Override
    public String getType() {
        return TYPE.getName();
    }

    public void countDown(){
        setCountDown(getCountDown() - 1);
        info.msg(getNameWithOwner() + "的倒数-1（剩余"+getCountDown()+"）");
        if(getCountDown() <= 0){
            death();
        }
    }

}
