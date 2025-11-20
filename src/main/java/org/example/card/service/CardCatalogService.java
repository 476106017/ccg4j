package org.example.card.service;

import org.example.card.Card;
import org.example.card.data.CardDataLoader;
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
import java.util.stream.Stream;

@Service
public class CardCatalogService {

    private volatile List<CardSummary> allCards;
    private volatile Map<String, CardSummary> cardByCode;

    public CardCatalogService() {
        // 延迟初始化，等待CardDataLoader加载完成
    }

    public List<CardSummary> getAllCards() {
        if (allCards == null) {
            synchronized (this) {
                if (allCards == null) {
                    List<CardSummary> cards = loadCards();
                    this.cardByCode = cards.stream()
                        .collect(Collectors.toUnmodifiableMap(CardSummary::getCode, c -> c));
                    this.allCards = Collections.unmodifiableList(cards);
                }
            }
        }
        return allCards;
    }

    public CardSummary getByCode(String code) {
        if (cardByCode == null) {
            getAllCards(); // 触发初始化
        }
        return cardByCode.get(code);
    }

    private List<CardSummary> loadCards() {
        // 加载Java类卡牌
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .forPackage("org.example.card"));
        List<CardSummary> javaCards = reflections.getSubTypesOf(Card.class).stream()
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
            .collect(Collectors.toList());
        
        // 加载数据驱动卡牌
        List<CardSummary> dataCards = CardDataLoader.getAllDataCardPrototypes().values().stream()
            .map(this::toSummaryFromCard)
            .filter(summary -> summary != null)
            .collect(Collectors.toList());
        
        // 合并并排序
        return Stream.concat(javaCards.stream(), dataCards.stream())
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
    
    private CardSummary toSummaryFromCard(Card prototype) {
        try {
            Integer countdown = null;
            Integer addAtk = null;
            if (prototype instanceof org.example.card.AmuletCard) {
                countdown = ((org.example.card.AmuletCard) prototype).getCountDown();
            } else if (prototype instanceof org.example.card.EquipmentCard) {
                org.example.card.EquipmentCard equipCard = (org.example.card.EquipmentCard) prototype;
                countdown = equipCard.getCountdown();
                addAtk = equipCard.getAddAtk();
            }
            // 使用卡牌名称作为code（数据驱动卡牌没有Class名称）
            String code = "data:" + prototype.getName();
            return CardSummary.builder()
                .code(code)
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
