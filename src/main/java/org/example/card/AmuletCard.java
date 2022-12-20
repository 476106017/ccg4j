package org.example.card;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.constant.CardType;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AmuletCard extends AreaCard{

    public final CardType TYPE = CardType.AMULET;
    public int count = 0;
    public int timer = 0;

    @Override
    public String getType() {
        return TYPE.getName();
    }

}
