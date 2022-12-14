package org.example.card;

import org.example.constant.CardType;
import org.example.game.GameInfo;

public abstract class SpellCard extends Card{
    public final CardType TYPE = CardType.SPELL;
    boolean grow = false;

    @Override
    public String getType() {
        return TYPE.getName();
    }

}
