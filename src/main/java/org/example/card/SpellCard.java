package org.example.card;

import org.example.constant.CardType;
import org.example.game.GameInfo;

public abstract class SpellCard extends Card{
    public final CardType type = CardType.SPELL;
    boolean grow = false;

}
