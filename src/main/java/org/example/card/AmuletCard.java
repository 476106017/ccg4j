package org.example.card;

import org.example.constant.CardType;
import org.example.game.GameInfo;

public abstract class AmuletCard extends Card{

    public final CardType TYPE = CardType.AMULET;

    @Override
    public String getType() {
        return TYPE.getName();
    }

}
