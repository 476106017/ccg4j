package org.example.game;

import lombok.Data;
import org.example.card.Card;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Data
public class PlayerDeck {
    List<Class<? extends Card>> activeDeck = new ArrayList<>();
    List<Class<? extends Card>> availableDeck = new ArrayList<>();

    public List<Card> getActiveDeckInstance(int owner, GameInfo info) {
        List<Card> _return = new ArrayList<>();
        activeDeck.forEach(cardClass->{
            try {
                Card card = cardClass.getDeclaredConstructor().newInstance();
                card.setOwner(owner);
                card.setInfo(info);
                card.initCounter();
                _return.add(card);
            } catch (NoSuchMethodException | InstantiationException |
                     IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
        return _return;
    }
}
