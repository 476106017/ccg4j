package org.example.card;

import org.example.constant.CardType;
import org.example.game.GameObj;

import java.util.List;

public abstract class SpellCard extends Card{
    public final CardType TYPE = CardType.SPELL;

    @Override
    public String getType() {
        return TYPE.getName();
    }

    @Override
    public void play(List<GameObj> targets) {
        super.play(targets);
        ownerPlayer().getGraveyard().add(this);
        ownerPlayer().countToGraveyard(1);
        ownerPlayer().getHand().remove(this);
    }
}
