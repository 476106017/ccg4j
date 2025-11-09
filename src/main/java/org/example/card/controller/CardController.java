package org.example.card.controller;

import lombok.RequiredArgsConstructor;
import org.example.card.dto.CardDisplayDto;
import org.example.card.dto.CardSummary;
import org.example.card.service.CardCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardCatalogService cardCatalogService;

    @GetMapping("/all")
    public List<CardDisplayDto> getAllCards() {
        return cardCatalogService.getAllCards().stream()
            .map(this::toDisplayDto)
            .collect(Collectors.toList());
    }

    @GetMapping("/{code}")
    public CardDisplayDto getCardByCode(@PathVariable String code) {
        CardSummary summary = cardCatalogService.getByCode(code);
        return summary != null ? toDisplayDto(summary) : null;
    }

    @GetMapping("/search")
    public List<CardDisplayDto> searchCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer cost) {

        List<CardSummary> results = cardCatalogService.getAllCards();

        if (name != null && !name.isEmpty()) {
            results = results.stream()
                .filter(c -> c.getName().contains(name))
                .collect(Collectors.toList());
        }

        if (cost != null) {
            results = results.stream()
                .filter(c -> c.getCost().equals(cost))
                .collect(Collectors.toList());
        }

        return results.stream()
            .map(this::toDisplayDto)
            .collect(Collectors.toList());
    }

    private CardDisplayDto toDisplayDto(CardSummary summary) {
        return CardDisplayDto.builder()
            .code(summary.getCode())
            .name(summary.getName())
            .cardType(summary.getType())
            .cost(summary.getCost())
            .job(summary.getJob())
            .race(summary.getRace())
            .mark(summary.getMark())
            .attack(summary.getAtk())
            .health(summary.getHp())
            .countdown(summary.getCountdown())
            .addAtk(summary.getAddAtk())
            .description("") // CardSummary 没有description字段，可能需要从Card实例获取
            .keywords(summary.getKeywords())
            .rarity(summary.getRarity())
            .build();
    }
}
