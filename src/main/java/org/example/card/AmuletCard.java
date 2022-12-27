package org.example.card;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardType;


@Getter
@Setter
public abstract class AmuletCard extends AreaCard{

    public final CardType TYPE = CardType.AMULET;
    public int count = -1;
    public int timer = -1;

    @Override
    public String getType() {
        return TYPE.getName();
    }

}
