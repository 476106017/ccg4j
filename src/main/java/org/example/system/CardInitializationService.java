package org.example.system;

import org.example.card.Card;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.FilterBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CardInitializationService {

    private List<Class<? extends Card>> allCardClasses = new ArrayList<>();
    private Map<Class<? extends Card>, Card> cardPrototypes = new ConcurrentHashMap<>();
    private Map<String, Class<? extends Card>> nameToCardClassMap = new ConcurrentHashMap<>();

    public List<Class<? extends Card>> getAllCardClasses() {
        return allCardClasses;
    }

    public Map<Class<? extends Card>, Card> getCardPrototypes() {
        return cardPrototypes;
    }

    public Map<String, Class<? extends Card>> getNameToCardClassMap() {
        return nameToCardClassMap;
    }

    @javax.annotation.PostConstruct
    public void initializeCards() {
        Reflections reflections = new Reflections("org.example.card",
            new SubTypesScanner(false),
            new TypeAnnotationsScanner(),
            new FilterBuilder().excludePackage("org.example.card.morecard"));

        allCardClasses = new ArrayList<>(reflections.getSubTypesOf(Card.class));
        allCardClasses.removeIf(cardClass -> 
            java.lang.reflect.Modifier.isAbstract(cardClass.getModifiers()) ||
            java.lang.reflect.Modifier.isStatic(cardClass.getModifiers()));

        for (Class<? extends Card> cardClass : allCardClasses) {
            try {
                Card card = cardClass.getDeclaredConstructor().newInstance();
                card.init();
                cardPrototypes.put(cardClass, card);
                nameToCardClassMap.put(card.getName(), cardClass);
            } catch (Exception e) {
                // Log the exception (e.g., using a logger)
                System.err.println("Error initializing card: " + cardClass.getName());
                e.printStackTrace();
            }
        }
    }
}
