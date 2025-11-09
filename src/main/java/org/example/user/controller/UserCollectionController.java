package org.example.user.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.auth.SessionConstants;
import org.example.card.dto.CardSummary;
import org.example.card.service.CardCatalogService;
import org.example.card.service.CardPackService;
import org.example.constant.CardRarity;
import org.example.user.dto.CardPackListResponse;
import org.example.user.dto.CardPackView;
import org.example.user.dto.CollectionCardResponse;
import org.example.user.dto.CollectionOverviewResponse;
import org.example.user.dto.OpenPackRequest;
import org.example.user.dto.OpenPackResponse;
import org.example.user.entity.UserAccount;
import org.example.user.entity.UserCardCollection;
import org.example.user.service.UserAccountService;
import org.example.user.service.UserCardCollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserCollectionController {

    private final UserAccountService userAccountService;
    private final UserCardCollectionService userCardCollectionService;
    private final CardCatalogService cardCatalogService;
    private final CardPackService cardPackService;

    @GetMapping("/collection")
    public CollectionOverviewResponse getCollection(HttpSession session) {
        UserAccount user = requireUser(session);
        Map<String, Integer> quantities = getUserCardQuantities(user.getId());
        List<CollectionCardResponse> cards = cardCatalogService.getAllCards().stream()
            .map(summary -> toCollectionCard(summary, quantities.getOrDefault(summary.getCode(), 0)))
            .collect(Collectors.toList());
        return CollectionOverviewResponse.builder()
            .tickets(user.getTickets())
            .arcaneDust(user.getArcaneDust())
            .cards(cards)
            .build();
    }

    @GetMapping("/packs")
    public CardPackListResponse getPacks(HttpSession session) {
        UserAccount user = requireUser(session);
        List<CardPackView> views = cardPackService.getAvailablePacks().stream()
            .map(def -> CardPackView.builder()
                .code(def.code())
                .name(def.name())
                .description(def.description())
                .build())
            .collect(Collectors.toList());
        return CardPackListResponse.builder()
            .tickets(user.getTickets())
            .packs(views)
            .build();
    }

    @PostMapping("/open-pack")
    public OpenPackResponse openPack(@Valid @RequestBody OpenPackRequest request, HttpSession session) {
        UserAccount user = requireUser(session);
        if (user.getTickets() == null || user.getTickets() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "抽奖券不足");
        }
        List<CardSummary> draws = cardPackService.openPack(request.getPackCode());
        if (draws.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该卡包暂时没有卡牌");
        }
        Map<String, Integer> additionCounts = new LinkedHashMap<>();
        draws.forEach(summary -> additionCounts.merge(summary.getCode(), 1, Integer::sum));

        Map<String, Integer> currentQuantities = getUserCardQuantities(user.getId());
        Map<String, Integer> updatedQuantities = new HashMap<>(currentQuantities);
        additionCounts.forEach((code, count) -> updatedQuantities.merge(code, count, Integer::sum));

        userCardCollectionService.addCards(user.getId(), additionCounts);
        user.setTickets(user.getTickets() - 1);
        user.setUpdatedAt(java.time.OffsetDateTime.now());
        userAccountService.updateById(user);

        List<CollectionCardResponse> cardResponses = draws.stream()
            .map(summary -> toCollectionCard(summary, updatedQuantities.getOrDefault(summary.getCode(), 0)))
            .collect(Collectors.toList());

        return OpenPackResponse.builder()
            .remainingTickets(user.getTickets())
            .cards(cardResponses)
            .build();
    }

    @PostMapping("/disenchant")
    public Map<String, Object> disenchantCard(@RequestParam String cardCode, HttpSession session) {
        UserAccount user = requireUser(session);
        
        // 查找卡牌信息
        CardSummary card = cardCatalogService.getAllCards().stream()
            .filter(c -> c.getCode().equals(cardCode))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "卡牌不存在"));
        
        // 获取用户拥有的卡牌数量
        Map<String, Integer> quantities = getUserCardQuantities(user.getId());
        Integer currentQuantity = quantities.getOrDefault(cardCode, 0);
        
        // 检查数量是否大于3
        if (currentQuantity <= 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "只能分解多余卡牌（数量>3）");
        }
        
        // 计算分解数量（超过3的部分全部分解）
        int disenchantCount = currentQuantity - 3;
        
        // 根据稀有度计算奥术之尘
        int dustPerCard = getDustValue(card.getRarity());
        int totalDust = dustPerCard * disenchantCount;
        
        // 更新用户卡牌数量
        userCardCollectionService.removeCards(user.getId(), Map.of(cardCode, disenchantCount));
        
        // 更新用户奥术之尘
        int currentDust = user.getArcaneDust() == null ? 0 : user.getArcaneDust();
        user.setArcaneDust(currentDust + totalDust);
        user.setUpdatedAt(java.time.OffsetDateTime.now());
        userAccountService.updateById(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("disenchantCount", disenchantCount);
        result.put("dustGained", totalDust);
        result.put("totalDust", user.getArcaneDust());
        result.put("remainingQuantity", 3);
        
        return result;
    }
    
    @PostMapping("/disenchant-all")
    public Map<String, Object> disenchantAllExcess(HttpSession session) {
        UserAccount user = requireUser(session);
        
        // 获取所有卡牌数量
        Map<String, Integer> quantities = getUserCardQuantities(user.getId());
        
        int totalDustGained = 0;
        int totalCardsDisenchanted = 0;
        Map<String, Integer> cardsToRemove = new HashMap<>();
        
        // 遍历所有卡牌，找出数量>3的
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String cardCode = entry.getKey();
            Integer quantity = entry.getValue();
            
            if (quantity > 3) {
                // 查找卡牌信息获取稀有度
                CardSummary card = cardCatalogService.getAllCards().stream()
                    .filter(c -> c.getCode().equals(cardCode))
                    .findFirst()
                    .orElse(null);
                
                if (card != null) {
                    int excessCount = quantity - 3;
                    int dustPerCard = getDustValue(card.getRarity());
                    int dustForThisCard = dustPerCard * excessCount;
                    
                    totalDustGained += dustForThisCard;
                    totalCardsDisenchanted += excessCount;
                    cardsToRemove.put(cardCode, excessCount);
                }
            }
        }
        
        if (cardsToRemove.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "没有多余的卡牌可分解");
        }
        
        // 批量移除卡牌
        userCardCollectionService.removeCards(user.getId(), cardsToRemove);
        
        // 更新用户奥术之尘
        int currentDust = user.getArcaneDust() == null ? 0 : user.getArcaneDust();
        user.setArcaneDust(currentDust + totalDustGained);
        user.setUpdatedAt(java.time.OffsetDateTime.now());
        userAccountService.updateById(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalCardsDisenchanted", totalCardsDisenchanted);
        result.put("totalDustGained", totalDustGained);
        result.put("totalDust", user.getArcaneDust());
        result.put("cardsProcessed", cardsToRemove.size());
        
        return result;
    }
    
    private int getDustValue(CardRarity rarity) {
        if (rarity == null) {
            return 50; // 默认铜卡
        }
        return switch (rarity) {
            case BRONZE -> 50;
            case SILVER -> 200;
            case GOLD -> 800;
            case RAINBOW -> 1600;
            case LEGENDARY -> 2400;
        };
    }

    private UserAccount requireUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(SessionConstants.SESSION_USER_ID);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        UserAccount user = userAccountService.getById(userId);
        if (user == null) {
            session.invalidate();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return user;
    }

    private Map<String, Integer> getUserCardQuantities(Long userId) {
        return userCardCollectionService.listByUserId(userId).stream()
            .collect(Collectors.toMap(UserCardCollection::getCardCode, UserCardCollection::getQuantity));
    }

    private CollectionCardResponse toCollectionCard(CardSummary summary, Integer quantity) {
        return CollectionCardResponse.builder()
            .code(summary.getCode())
            .name(summary.getName())
            .type(summary.getType())
            .cost(summary.getCost())
            .job(summary.getJob())
            .race(summary.getRace())
            .keywords(summary.getKeywords())
            .mark(summary.getMark())
            .rarity(summary.getRarity() == null ? null : summary.getRarity().name())
            .quantity(quantity == null ? 0 : quantity)
            .atk(summary.getAtk())
            .hp(summary.getHp())
            .build();
    }
}
