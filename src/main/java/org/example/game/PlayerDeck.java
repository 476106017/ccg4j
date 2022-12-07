package org.example.game;

import lombok.Data;
import org.example.card.Card;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlayerDeck {
    List<Card> activeDeck = new ArrayList<>();
    List<Card> availableDeck = new ArrayList<>();

}
