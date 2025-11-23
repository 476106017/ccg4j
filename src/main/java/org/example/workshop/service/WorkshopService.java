package org.example.workshop.service;

import org.example.workshop.entity.WorkshopCard;
import org.example.workshop.entity.WorkshopComment;

import java.util.List;
import java.util.Map;

public interface WorkshopService {
    List<Map<String, Object>> getCards(String status, String sortBy);

    Map<String, Object> getCardDetail(Long cardId, Long userId);

    void createCard(WorkshopCard card, Long userId);

    void voteCard(Long cardId, Long userId);

    void addComment(Long cardId, String content, Long userId);

    void implementCard(Long cardId, Long userId);

    void deleteCard(Long cardId, Long userId);
}
