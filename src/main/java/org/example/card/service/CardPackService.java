package org.example.card.service;

import lombok.RequiredArgsConstructor;
import org.example.card.dto.CardSummary;
import org.example.constant.CardRarity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardPackService {

    private static final int PACK_SIZE = 10;

    private final CardCatalogService cardCatalogService;
    private final Random random = new SecureRandom();

    private final Map<String, CardPackDefinition> packDefinitions = initDefinitions();

    // 基础抽取权重（越大越常见）
    private static final Map<CardRarity, Integer> BASE_WEIGHTS = new EnumMap<>(CardRarity.class);
    static {
        BASE_WEIGHTS.put(CardRarity.BRONZE, 85);
        BASE_WEIGHTS.put(CardRarity.SILVER, 12);
        BASE_WEIGHTS.put(CardRarity.GOLD, 2);
        BASE_WEIGHTS.put(CardRarity.RAINBOW, 1);
        BASE_WEIGHTS.put(CardRarity.LEGENDARY, 0); // 极低，通常通过保底/权重上调获得
    }

    public List<CardPackDefinition> getAvailablePacks() {
        return List.copyOf(packDefinitions.values());
    }

    public CardPackDefinition getPack(String code) {
        return Optional.ofNullable(packDefinitions.get(code))
            .orElseThrow(() -> new IllegalArgumentException("未知的卡包：" + code));
    }

    public List<CardSummary> openPack(String code) {
        CardPackDefinition definition = getPack(code);
        List<CardSummary> pool = cardCatalogService.getAllCards().stream()
            .filter(definition::matches)
            .collect(Collectors.toList());
        if (pool.isEmpty()) {
            throw new IllegalStateException("该卡包暂时没有可用卡牌：" + code);
        }

        // 按稀有度分组
        Map<CardRarity, List<CardSummary>> byRarity = pool.stream()
            .collect(Collectors.groupingBy(CardSummary::getRarity, () -> new EnumMap<>(CardRarity.class), Collectors.toList()));

        List<CardSummary> result = new ArrayList<>(PACK_SIZE);

        // 先抽取 PACK_SIZE-1 张，按基础权重
        for (int i = 0; i < PACK_SIZE - 1; i++) {
            CardSummary pick = pickOne(byRarity, BASE_WEIGHTS);
            if (pick != null) result.add(pick);
        }

        // 最后一张：保底为银卡或以上
        Map<CardRarity, Integer> guaranteedWeights = new EnumMap<>(CardRarity.class);
        guaranteedWeights.put(CardRarity.SILVER, 80);
        guaranteedWeights.put(CardRarity.GOLD, 15);
        guaranteedWeights.put(CardRarity.RAINBOW, 3);
        guaranteedWeights.put(CardRarity.LEGENDARY, 2);
        CardSummary guaranteed = pickOne(byRarity, guaranteedWeights);
        if (guaranteed == null) {
            // 如果池子没有银卡以上，则退化为基础抽取
            guaranteed = pickOne(byRarity, BASE_WEIGHTS);
        }
        if (guaranteed != null) result.add(guaranteed);

        // 如果极端情况下仍没有银卡以上，尝试替换一张为银卡（若存在）
        boolean hasSilverPlus = result.stream().anyMatch(s -> s.getRarity().ordinal() >= CardRarity.SILVER.ordinal());
        if (!hasSilverPlus) {
            List<CardSummary> silverPlusPool = new ArrayList<>();
            for (CardRarity r : List.of(CardRarity.SILVER, CardRarity.GOLD, CardRarity.RAINBOW, CardRarity.LEGENDARY)) {
                silverPlusPool.addAll(byRarity.getOrDefault(r, List.of()));
            }
            if (!silverPlusPool.isEmpty()) {
                CardSummary replacement = silverPlusPool.get(random.nextInt(silverPlusPool.size()));
                if (!result.isEmpty()) result.set(random.nextInt(result.size()), replacement);
                else result.add(replacement);
            }
        }

        return result;
    }

    private CardSummary pickOne(Map<CardRarity, List<CardSummary>> byRarity, Map<CardRarity, Integer> weights) {
        // 根据权重与池可用性进行抽取
        int total = 0;
        for (Map.Entry<CardRarity, Integer> e : weights.entrySet()) {
            if (e.getValue() > 0 && byRarity.getOrDefault(e.getKey(), List.of()).size() > 0) {
                total += e.getValue();
            }
        }
        if (total <= 0) return null;
        int rnd = random.nextInt(total);
        int acc = 0;
        for (Map.Entry<CardRarity, Integer> e : weights.entrySet()) {
            int w = e.getValue();
            if (w <= 0) continue;
            List<CardSummary> list = byRarity.getOrDefault(e.getKey(), List.of());
            if (list.isEmpty()) continue;
            acc += w;
            if (rnd < acc) {
                return list.get(random.nextInt(list.size()));
            }
        }
        // 兜底：任一可用池
        List<CardSummary> fallback = byRarity.values().stream().filter(l -> !l.isEmpty()).findAny().orElse(List.of());
        return fallback.isEmpty() ? null : fallback.get(random.nextInt(fallback.size()));
    }

    private Map<String, CardPackDefinition> initDefinitions() {
        Map<String, CardPackDefinition> map = new LinkedHashMap<>();
        map.put("ccg_neutral", new CardPackDefinition(
            "ccg_neutral",
            "中立纪念卡包",
            "收录CCG系列的中立随从与法术",
            summary -> summary.getCode().contains(".card.ccg.neutral.")
        ));
        map.put("ccg_fairy", new CardPackDefinition(
            "ccg_fairy",
            "妖精森林包",
            "来自妖精职业的经典卡牌",
            summary -> summary.getCode().contains(".card.ccg.fairy.")
        ));
        map.put("ccg_druid", new CardPackDefinition(
            "ccg_druid",
            "德鲁伊秘典",
            "大自然的力量与伙伴",
            summary -> summary.getCode().contains(".card.ccg.druid.")
        ));
        map.put("festival", new CardPackDefinition(
            "festival",
            "节日狂欢包",
            "节日限定的趣味卡牌合集",
            summary -> summary.getCode().contains(".card.ccg.festival.")
        ));
        map.put("nemesis", new CardPackDefinition(
            "nemesis",
            "复合兵装包",
            "复合兵装与神器组合",
            summary -> summary.getCode().contains(".card.ccg.nemesis.")
        ));
        map.put("anime", new CardPackDefinition(
            "anime",
            "次元来客",
            "跨作品特别联动卡牌",
            summary -> summary.getCode().contains(".card.anime.")
        ));
        map.put("original", new CardPackDefinition(
            "original",
            "原创收藏",
            "原创主题与自制卡牌合集",
            summary -> summary.getCode().contains(".card.original.")
        ));
        return Collections.unmodifiableMap(map);
    }
}
