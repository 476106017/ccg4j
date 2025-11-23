package org.example.workshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.user.entity.UserAccount;
import org.example.user.service.UserAccountService;
import org.example.workshop.entity.WorkshopCard;
import org.example.workshop.entity.WorkshopComment;
import org.example.workshop.entity.WorkshopVote;
import org.example.workshop.mapper.WorkshopCommentMapper;
import org.example.workshop.mapper.WorkshopMapper;
import org.example.workshop.mapper.WorkshopVoteMapper;
import org.example.workshop.service.WorkshopService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkshopServiceImpl implements WorkshopService {

    private final WorkshopMapper workshopMapper;
    private final WorkshopCommentMapper commentMapper;
    private final WorkshopVoteMapper voteMapper;
    private final UserAccountService userAccountService;

    private static final String ADMIN_USERNAME = "admin";

    private boolean isAdmin(Long userId) {
        UserAccount user = userAccountService.getById(userId);
        return user != null && ADMIN_USERNAME.equals(user.getUsername());
    }

    @Override
    public List<Map<String, Object>> getCards(String status, String sortBy) {
        LambdaQueryWrapper<WorkshopCard> query = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            query.eq(WorkshopCard::getStatus, status);
        }

        if ("likes".equals(sortBy)) {
            query.orderByDesc(WorkshopCard::getLikes);
        } else {
            query.orderByDesc(WorkshopCard::getCreatedAt);
        }

        List<WorkshopCard> cards = workshopMapper.selectList(query);

        List<Long> authorIds = cards.stream().map(WorkshopCard::getAuthorId).collect(Collectors.toList());
        Map<Long, String> userNames = getUserNames(authorIds);

        return cards.stream().map(card -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", card.getId());
            map.put("name", card.getName());
            map.put("description", card.getDescription());
            map.put("cost", card.getCost());
            map.put("attack", card.getAttack());
            map.put("health", card.getHealth());
            map.put("cardType", card.getCardType());
            map.put("job", card.getJob());
            map.put("race", card.getRace());
            map.put("countdown", card.getCountdown());
            map.put("likes", card.getLikes());
            map.put("status", card.getStatus());
            map.put("authorId", card.getAuthorId());
            map.put("authorName", userNames.getOrDefault(card.getAuthorId(), "Unknown"));
            map.put("createdAt", card.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }

    private Map<Long, String> getUserNames(List<Long> userIds) {
        if (userIds.isEmpty())
            return new HashMap<>();
        List<UserAccount> users = userAccountService.listByIds(userIds);
        return users.stream().collect(Collectors.toMap(UserAccount::getId, UserAccount::getUsername));
    }

    @Override
    public Map<String, Object> getCardDetail(Long cardId, Long userId) {
        WorkshopCard card = workshopMapper.selectById(cardId);
        if (card == null)
            throw new RuntimeException("卡牌不存在");

        Map<String, Object> result = new HashMap<>();
        result.put("card", card);
        result.put("authorName", userAccountService.getById(card.getAuthorId()).getUsername());

        // Comments
        List<WorkshopComment> comments = commentMapper.selectList(new LambdaQueryWrapper<WorkshopComment>()
                .eq(WorkshopComment::getCardId, cardId)
                .orderByDesc(WorkshopComment::getCreatedAt));

        List<Long> commentAuthorIds = comments.stream().map(WorkshopComment::getAuthorId).collect(Collectors.toList());
        Map<Long, String> commentUserNames = getUserNames(commentAuthorIds);

        List<Map<String, Object>> commentList = comments.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("content", c.getContent());
            map.put("authorName", commentUserNames.getOrDefault(c.getAuthorId(), "Unknown"));
            map.put("createdAt", c.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        result.put("comments", commentList);

        // User vote status
        if (userId != null) {
            Long count = voteMapper.selectCount(new LambdaQueryWrapper<WorkshopVote>()
                    .eq(WorkshopVote::getUserId, userId)
                    .eq(WorkshopVote::getCardId, cardId));
            result.put("hasVoted", count > 0);
        }

        // Is Admin
        if (userId != null) {
            result.put("isAdmin", isAdmin(userId));
            result.put("currentUserId", userId);
        }

        return result;
    }

    @Override
    @Transactional
    public void createCard(WorkshopCard card, Long userId) {
        card.setAuthorId(userId);
        card.setStatus("SUBMITTED");
        card.setLikes(0);
        card.setCreatedAt(OffsetDateTime.now());
        card.setUpdatedAt(OffsetDateTime.now());
        workshopMapper.insert(card);
    }

    @Override
    @Transactional
    public void voteCard(Long cardId, Long userId) {
        WorkshopCard card = workshopMapper.selectById(cardId);
        if (card == null)
            throw new RuntimeException("卡牌不存在");

        WorkshopVote existingVote = voteMapper.selectOne(new LambdaQueryWrapper<WorkshopVote>()
                .eq(WorkshopVote::getUserId, userId)
                .eq(WorkshopVote::getCardId, cardId));

        if (existingVote != null) {
            // Cancel vote
            voteMapper.deleteById(existingVote.getId());
            card.setLikes(Math.max(0, card.getLikes() - 1));
        } else {
            // Add vote
            WorkshopVote vote = new WorkshopVote();
            vote.setUserId(userId);
            vote.setCardId(cardId);
            vote.setCreatedAt(OffsetDateTime.now());
            voteMapper.insert(vote);
            card.setLikes(card.getLikes() + 1);
        }
        workshopMapper.updateById(card);
    }

    @Override
    @Transactional
    public void addComment(Long cardId, String content, Long userId) {
        WorkshopCard card = workshopMapper.selectById(cardId);
        if (card == null)
            throw new RuntimeException("卡牌不存在");

        WorkshopComment comment = new WorkshopComment();
        comment.setCardId(cardId);
        comment.setAuthorId(userId);
        comment.setContent(content);
        comment.setCreatedAt(OffsetDateTime.now());
        commentMapper.insert(comment);
    }

    @Override
    @Transactional
    public void implementCard(Long cardId, Long userId) {
        if (!isAdmin(userId)) {
            throw new RuntimeException("只有管理员可以执行此操作");
        }

        WorkshopCard card = workshopMapper.selectById(cardId);
        if (card == null)
            throw new RuntimeException("卡牌不存在");

        card.setStatus("IMPLEMENTED");
        card.setUpdatedAt(OffsetDateTime.now());
        workshopMapper.updateById(card);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId, Long userId) {
        WorkshopCard card = workshopMapper.selectById(cardId);
        if (card == null)
            throw new RuntimeException("卡牌不存在");

        if (!card.getAuthorId().equals(userId) && !isAdmin(userId)) {
            throw new RuntimeException("无权操作");
        }

        workshopMapper.deleteById(cardId);
    }
}
