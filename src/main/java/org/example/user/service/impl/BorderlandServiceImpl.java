package org.example.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.card.dto.CardSummary;
import org.example.card.service.CardCatalogService;
import org.example.user.entity.BorderlandExportRecord;
import org.example.user.entity.BorderlandVisa;
import org.example.user.mapper.BorderlandExportRecordMapper;
import org.example.user.mapper.BorderlandVisaMapper;
import org.example.user.service.BorderlandService;
import org.example.user.service.UserCardCollectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorderlandServiceImpl implements BorderlandService {
    
    private final BorderlandVisaMapper visaMapper;
    private final BorderlandExportRecordMapper exportRecordMapper;
    private final UserCardCollectionService userCardCollectionService;
    private final CardCatalogService cardCatalogService;
    
    @Override
    public BorderlandVisa getVisaStatus(Long userId) {
        QueryWrapper<BorderlandVisa> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        qw.orderByDesc("created_at");
        qw.last("LIMIT 1");
        
        BorderlandVisa visa = visaMapper.selectOne(qw);
        if (visa == null) {
            return null;
        }
        
        // 检查是否过期或惩罚期结束
        if ("PUNISHED".equals(visa.getStatus())) {
            if (visa.getPunishmentEndTime() != null && 
                OffsetDateTime.now().isAfter(visa.getPunishmentEndTime())) {
                // 惩罚期结束，状态改为EXPIRED
                visa.setStatus("EXPIRED");
                visa.setUpdatedAt(OffsetDateTime.now());
                visaMapper.updateById(visa);
            }
        }
        
        return visa;
    }
    
    @Override
    @Transactional
    public BorderlandVisa applyVisa(Long userId) {
        // 检查是否在惩罚期
        BorderlandVisa currentVisa = getVisaStatus(userId);
        if (currentVisa != null && "PUNISHED".equals(currentVisa.getStatus())) {
            if (currentVisa.getPunishmentEndTime() != null && 
                OffsetDateTime.now().isBefore(currentVisa.getPunishmentEndTime())) {
                throw new RuntimeException("还在惩罚期内，无法办理新签证");
            }
        }
        
        // 生成40张随机卡组
        String deckData = generateRandomDeck(40);
        
        BorderlandVisa visa = new BorderlandVisa();
        visa.setUserId(userId);
        visa.setStatus("ACTIVE");
        visa.setDaysRemaining(10);
        visa.setDeckData(deckData);
        visa.setCreatedAt(OffsetDateTime.now());
        visa.setUpdatedAt(OffsetDateTime.now());
        
        visaMapper.insert(visa);
        
        log.info("用户 {} 办理签证成功，获得卡组: {}", userId, deckData);
        return visa;
    }
    
    @Override
    @Transactional
    public BorderlandVisa discardCard(Long userId, String cardCode) {
        BorderlandVisa visa = getVisaStatus(userId);
        if (visa == null || !"ACTIVE".equals(visa.getStatus())) {
            throw new RuntimeException("没有有效的签证");
        }
        
        // 从卡组中移除一张指定卡牌
        List<String> cards = new ArrayList<>(Arrays.asList(visa.getDeckData().split(",")));
        boolean removed = cards.remove(cardCode);
        
        if (!removed) {
            throw new RuntimeException("卡组中没有这张卡牌");
        }
        
        visa.setDeckData(String.join(",", cards));
        visa.setUpdatedAt(OffsetDateTime.now());
        visaMapper.updateById(visa);
        
        log.info("用户 {} 丢弃卡牌 {}, 剩余 {} 张", userId, cardCode, cards.size());
        return visa;
    }
    
    @Override
    @Transactional
    public void settleBattle(Long winnerId, Long loserId, boolean isAchievement) {
        BorderlandVisa winnerVisa = getVisaStatus(winnerId);
        BorderlandVisa loserVisa = getVisaStatus(loserId);
        
        if (winnerVisa == null || loserVisa == null) {
            throw new RuntimeException("参赛者签证无效");
        }
        
        // 胜者获取败者的所有卡牌和剩余天数
        List<String> winnerCards = Arrays.asList(winnerVisa.getDeckData().split(","));
        List<String> loserCards = Arrays.asList(loserVisa.getDeckData().split(","));
        
        List<String> mergedCards = new ArrayList<>();
        mergedCards.addAll(winnerCards);
        mergedCards.addAll(loserCards);
        
        winnerVisa.setDeckData(String.join(",", mergedCards));
        winnerVisa.setDaysRemaining(winnerVisa.getDaysRemaining() + loserVisa.getDaysRemaining());
        winnerVisa.setUpdatedAt(OffsetDateTime.now());
        visaMapper.updateById(winnerVisa);
        
        // 败者进入24小时惩罚期
        loserVisa.setStatus("PUNISHED");
        loserVisa.setDeckData("");
        loserVisa.setDaysRemaining(0);
        loserVisa.setPunishmentEndTime(OffsetDateTime.now().plusHours(24));
        loserVisa.setUpdatedAt(OffsetDateTime.now());
        visaMapper.updateById(loserVisa);
        
        log.info("弥留之国对战结算: 胜者 {} 获得 {} 张卡和 {} 天，败者 {} 进入惩罚期", 
            winnerId, loserCards.size(), loserVisa.getDaysRemaining(), loserId);
    }
    
    @Override
    @Transactional
    public void winAgainstAI(Long userId) {
        BorderlandVisa visa = getVisaStatus(userId);
        if (visa == null || !"ACTIVE".equals(visa.getStatus())) {
            log.warn("用户 {} 没有有效签证，无法增加天数", userId);
            return;
        }
        
        // 击败AI增加1天签证时长
        visa.setDaysRemaining(visa.getDaysRemaining() + 1);
        visa.setUpdatedAt(OffsetDateTime.now());
        visaMapper.updateById(visa);
        
        log.info("用户 {} 击败AI，签证延长1天，当前剩余 {} 天", userId, visa.getDaysRemaining());
    }
    
    @Override
    @Transactional
    public String winAgainstAI(Long userId, List<String> aiDeckCards) {
        BorderlandVisa visa = getVisaStatus(userId);
        if (visa == null || !"ACTIVE".equals(visa.getStatus())) {
            log.warn("用户 {} 没有有效签证，无法获得卡牌", userId);
            return null;
        }
        
        // 获取玩家当前卡组
        List<String> currentCards = new ArrayList<>();
        if (visa.getDeckData() != null && !visa.getDeckData().isEmpty()) {
            currentCards.addAll(Arrays.asList(visa.getDeckData().split(",")));
        }
        
        // 从AI的卡牌中随机选择1张
        String rewardCard = null;
        String rewardCardName = null;
        if (aiDeckCards != null && !aiDeckCards.isEmpty()) {
            Random random = new Random();
            rewardCard = aiDeckCards.get(random.nextInt(aiDeckCards.size()));
            currentCards.add(rewardCard);
            
            // 获取卡牌名称
            try {
                CardSummary cardSummary = cardCatalogService.getByCode(rewardCard);
                if (cardSummary != null) {
                    rewardCardName = cardSummary.getName();
                }
            } catch (Exception e) {
                log.warn("获取卡牌名称失败: {}", rewardCard, e);
                rewardCardName = rewardCard; // 如果获取失败，使用代码作为后备
            }
        }
        
        // 更新卡组数据和签证天数
        visa.setDeckData(String.join(",", currentCards));
        visa.setDaysRemaining(visa.getDaysRemaining() + 1);
        visa.setUpdatedAt(OffsetDateTime.now());
        visaMapper.updateById(visa);
        
        log.info("用户 {} 击败AI，随机获得1张卡牌 [{}]，签证延长1天，当前剩余 {} 天", 
            userId, rewardCardName != null ? rewardCardName : "无", visa.getDaysRemaining());
        
        return rewardCardName != null ? rewardCardName : rewardCard;
    }
    
    @Override
    @Transactional
    public void loseAgainstAI(Long userId) {
        BorderlandVisa visa = getVisaStatus(userId);
        if (visa == null || !"ACTIVE".equals(visa.getStatus())) {
            log.warn("用户 {} 没有有效签证，无法进入惩罚期", userId);
            return;
        }
        
        // 清空卡组
        visa.setDeckData("");
        visa.setDaysRemaining(0);
        visa.setStatus("PUNISHED");
        // 测试期间惩罚1分钟
        visa.setPunishmentEndTime(OffsetDateTime.now().plusMinutes(1));
        visa.setUpdatedAt(OffsetDateTime.now());
        visaMapper.updateById(visa);
        
        log.info("用户 {} 输给AI，签证失效，卡组清空，进入1分钟惩罚期", userId);
    }
    
    @Override
    @Transactional
    public void exportCard(Long userId, String cardCode) {
        BorderlandVisa visa = getVisaStatus(userId);
        if (visa == null || !"ACTIVE".equals(visa.getStatus())) {
            throw new RuntimeException("没有有效的签证");
        }
        
        // 检查是否有这张卡
        List<String> cards = Arrays.asList(visa.getDeckData().split(","));
        if (!cards.contains(cardCode)) {
            throw new RuntimeException("卡组中没有这张卡牌");
        }
        
        // 添加到用户收藏
        Map<String, Integer> cardToAdd = new HashMap<>();
        cardToAdd.put(cardCode, 1);
        userCardCollectionService.addCards(userId, cardToAdd);
        
        // 记录带出记录
        BorderlandExportRecord record = new BorderlandExportRecord();
        record.setUserId(userId);
        record.setCardCode(cardCode);
        record.setVisaId(visa.getId());
        record.setCreatedAt(OffsetDateTime.now());
        exportRecordMapper.insert(record);
        
        // 签证失效
        visa.setStatus("EXPIRED");
        visa.setUpdatedAt(OffsetDateTime.now());
        visaMapper.updateById(visa);
        
        log.info("用户 {} 带出卡牌 {} 到收藏", userId, cardCode);
    }
    
    /**
     * 生成随机卡组
     */
    private String generateRandomDeck(int count) {
        List<CardSummary> allCards = cardCatalogService.getAllCards();
        
        if (allCards.isEmpty()) {
            throw new RuntimeException("卡池为空");
        }
        
        Random random = new Random();
        List<String> deckCodes = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            CardSummary randomCard = allCards.get(random.nextInt(allCards.size()));
            deckCodes.add(randomCard.getCode());
        }
        
        return String.join(",", deckCodes);
    }
}
