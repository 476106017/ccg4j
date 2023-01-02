package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardType;


@Getter
@Setter
public abstract class AmuletCard extends AreaCard{

    public final CardType TYPE = CardType.AMULET;
    public int countDown = -1;

    @Override
    public String getType() {
        return TYPE.getName();
    }

}
