package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.system.Database;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Getter
@Setter
public class PlayerDeck {
    Class<? extends Leader> leaderClass;
    List<Class<? extends Card>> activeDeck = new ArrayList<>();
    List<Class<? extends Card>> availableDeck = new ArrayList<>();

    public Leader getLeader(int owner, GameInfo info){
        try {
            Leader leader = leaderClass.getDeclaredConstructor().newInstance();
            leader.setOwner(owner);
            leader.setInfo(info);
            leader.init();
            return leader;
        } catch (NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Card> getActiveDeckInstance(int owner, GameInfo info) {
        List<Card> _return = new ArrayList<>();
        activeDeck.forEach(cardClass->{
            try {
                Card card = cardClass.getDeclaredConstructor().newInstance();
                card.setOwner(owner);
                card.setInfo(info);
                card.init();
                _return.add(card);
            } catch (NoSuchMethodException | InstantiationException |
                     IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
        return _return;
    }

    public Map describe() {
        Map<String,Object> _return = new HashMap<>();
        try {
            Leader leader = leaderClass.getDeclaredConstructor().newInstance();
            _return.put("leader", leader);
        }catch (Exception ignored){}

        List<? extends Card> deckCards = activeDeck.stream()
            .map(Database::getPrototype).sorted(Comparator.comparing(Card::getCost)).toList();
        _return.put("deck", deckCards);
        return _return;
    }
}
