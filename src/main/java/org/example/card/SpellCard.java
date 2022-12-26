package org.example.card;

import org.example.constant.CardType;

public abstract class SpellCard extends Card{
    public final CardType TYPE = CardType.SPELL;

    @Override
    public String getType() {
        return TYPE.getName();
    }

}
