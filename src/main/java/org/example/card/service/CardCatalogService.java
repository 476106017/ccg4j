package org.example.card.service;

import org.example.card.Card;
import org.example.card.dto.CardSummary;
import org.example.system.Database;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CardCatalogService {

    private final List<CardSummary> allCards;
    private final Map<String, CardSummary> cardByCode;

    public CardCatalogService() {
        this.allCards = Collections.unmodifiableList(loadCards());
        this.cardByCode = allCards.stream()
            .collect(Collectors.toUnmodifiableMap(CardSummary::getCode, c -> c));
    }

    public List<CardSummary> getAllCards() {
        return allCards;
    }

    public CardSummary getByCode(String code) {
        return cardByCode.get(code);
    }

    private List<CardSummary> loadCards() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .forPackage("org.example.card"));
        return reflections.getSubTypesOf(Card.class).stream()
            .filter(aClass -> {
                int modifiers = aClass.getModifiers();
                // 排除抽象类、接口和衍生物卡（_derivant 包下的卡）
                boolean isDerivant = aClass.getName().contains("._derivant.");
                // 排除内部类（通常是衍生物，如 TreasureLost）
                boolean isInnerClass = aClass.getEnclosingClass() != null;
                return !Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers) 
                    && !isDerivant && !isInnerClass;
            })
            .map(this::toSummarySafely)
            .filter(summary -> summary != null)
            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .collect(Collectors.toList());
    }

    private CardSummary toSummarySafely(Class<? extends Card> clazz) {
        try {
            Card prototype = Database.getPrototype(clazz);
            Integer countdown = null;
            Integer addAtk = null;
            if (prototype instanceof org.example.card.AmuletCard) {
                countdown = ((org.example.card.AmuletCard) prototype).getCountDown();
            } else if (prototype instanceof org.example.card.EquipmentCard) {
                org.example.card.EquipmentCard equipCard = (org.example.card.EquipmentCard) prototype;
                countdown = equipCard.getCountdown();
                addAtk = equipCard.getAddAtk();
            }
            return CardSummary.builder()
                .code(clazz.getName())
                .name(prototype.getName())
                .type(prototype.getType())
                .cost(prototype.getCost())
                .job(prototype.getJob())
                .race(new ArrayList<>(prototype.getRace()))
                .keywords(new ArrayList<>(prototype.getKeywords()))
                .mark(prototype.getMark())
                .atk(prototype.getAtkAsInteger())
                .hp(prototype.getHpAsInteger())
                .countdown(countdown)
                .addAtk(addAtk)
                .rarity(prototype.getRarity())
                .build();
        } catch (Exception e) {
            return null;
        }
    }
}
