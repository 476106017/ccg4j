package org.example.user.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.auth.SessionConstants;
import org.example.card.dto.CardSummary;
import org.example.card.service.CardCatalogService;
import org.example.constant.CardRarity;
import org.example.user.dto.CreateDeckRequest;
import org.example.user.dto.DeckResponse;
import org.example.user.dto.UpdateDeckRequest;
import org.example.user.entity.UserAccount;
import org.example.user.entity.UserDeck;
import org.example.user.service.UserAccountService;
import org.example.user.service.UserDeckService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/deck")
@RequiredArgsConstructor
public class UserDeckController {

    private final UserDeckService userDeckService;
    private final UserAccountService userAccountService;
    private final CardCatalogService cardCatalogService;

    @GetMapping("/list")
    public List<DeckResponse> listDecks(HttpSession session) {
        UserAccount user = requireUser(session);
        List<UserDeck> decks = userDeckService.listByUserId(user.getId());
        
        return decks.stream()
            .map(this::toDeckResponse)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public DeckResponse getDeck(@PathVariable Long id, HttpSession session) {
        UserAccount user = requireUser(session);
        UserDeck deck = userDeckService.lambdaQuery()
            .eq(UserDeck::getId, id)
            .eq(UserDeck::getUserId, user.getId())
            .one();
        
        if (deck == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "卡组不存在");
        }
        
        return toDeckResponse(deck);
    }

    @PostMapping("/create")
    public DeckResponse createDeck(@Valid @RequestBody CreateDeckRequest request, HttpSession session) {
        UserAccount user = requireUser(session);
        UserDeck deck = userDeckService.createDeck(user.getId(), request.getDeckName(), request.getDeckData());
        return toDeckResponse(deck);
    }

    @PutMapping("/{id}")
    public DeckResponse updateDeck(@PathVariable Long id, 
                                   @RequestBody UpdateDeckRequest request, 
                                   HttpSession session) {
        UserAccount user = requireUser(session);
        UserDeck deck = userDeckService.updateDeck(id, user.getId(), request.getDeckName(), request.getDeckData());
        
        if (deck == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "卡组不存在");
        }
        
        return toDeckResponse(deck);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteDeck(@PathVariable Long id, HttpSession session) {
        UserAccount user = requireUser(session);
        boolean deleted = userDeckService.deleteDeck(id, user.getId());
        
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "卡组不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
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

    private DeckResponse toDeckResponse(UserDeck deck) {
        // 解析卡组数据
        String[] cardCodes = deck.getDeckData() == null || deck.getDeckData().isEmpty() 
            ? new String[0] 
            : deck.getDeckData().split(",");
        
        // 计算总尘数
        int totalDust = calculateTotalDust(cardCodes);
        
        return DeckResponse.builder()
            .id(deck.getId())
            .deckName(deck.getDeckName())
            .deckData(deck.getDeckData())
            .totalDust(totalDust)
            .cardCount(cardCodes.length)
            .createdAt(deck.getCreatedAt() == null ? null : deck.getCreatedAt().toString())
            .updatedAt(deck.getUpdatedAt() == null ? null : deck.getUpdatedAt().toString())
            .build();
    }

    private int calculateTotalDust(String[] cardCodes) {
        if (cardCodes == null || cardCodes.length == 0) {
            return 0;
        }
        
        // 获取所有卡牌信息
        List<CardSummary> allCards = cardCatalogService.getAllCards();
        Map<String, CardSummary> cardMap = allCards.stream()
            .collect(Collectors.toMap(CardSummary::getCode, c -> c, (a, b) -> a));
        
        return Arrays.stream(cardCodes)
            .map(String::trim)
            .filter(code -> !code.isEmpty())
            .map(cardMap::get)
            .filter(card -> card != null)
            .mapToInt(card -> getDustValue(card.getRarity()))
            .sum();
    }

    private int getDustValue(CardRarity rarity) {
        if (rarity == null) {
            return 100; // 默认铜卡
        }
        return switch (rarity) {
            case BRONZE -> 100;
            case SILVER -> 400;
            case GOLD -> 800;
            case RAINBOW -> 1600;
            case LEGENDARY -> 3200;
        };
    }
}
