package org.example.card.service;

import java.util.List;
import java.util.function.Predicate;

import org.example.card.dto.CardSummary;

public record CardPackDefinition(
    String code,
    String name,
    String description,
    Predicate<CardSummary> filter
) {
    public boolean matches(CardSummary summary) {
        return filter.test(summary);
    }
}
