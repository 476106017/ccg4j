package org.example.card.genshin;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;

import java.util.List;

@Getter
@Setter
public abstract class ElementCostSpellCard extends SpellCard {
    List<Elemental> getElementCost;
}
