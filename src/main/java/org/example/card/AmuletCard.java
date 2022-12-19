package org.example.card;

import org.example.constant.CardType;

public abstract class AmuletCard extends Card{

    public final CardType TYPE = CardType.AMULET;
    public int count = 0;
    public int timer = 0;

    @Override
    public String getType() {
        return TYPE.getName();
    }

}
