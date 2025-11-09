package org.example.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.user.entity.UserAccount;
import org.example.user.mapper.UserAccountMapper;
import org.example.user.service.RatingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    
    private final UserAccountMapper userAccountMapper;
    
    @Override
    public Integer getRating(Long userId) {
        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            return 1000; // 默认分数
        }
        return user.getMatchRating() != null ? user.getMatchRating() : 1000;
    }
    
    @Override
    @Transactional
    public int[] settleBattle(Long winnerId, Long loserId) {
        UserAccount winner = userAccountMapper.selectById(winnerId);
        UserAccount loser = userAccountMapper.selectById(loserId);
        
        int winnerRating = winner.getMatchRating() != null ? winner.getMatchRating() : 1000;
        int loserRating = loser.getMatchRating() != null ? loser.getMatchRating() : 1000;
        
        // 计算分数变化（改进的ELO系统）
        // 分差越大，获得的分数越多
        int ratingDiff = loserRating - winnerRating;
        
        // 基础分数
        int basePoints = 25;
        
        // 根据分差调整
        int transferPoints;
        if (ratingDiff > 0) {
            // 胜者分数低于败者，奖励更多分数
            // 每相差100分，额外奖励5分
            transferPoints = basePoints + Math.min(50, (ratingDiff / 100) * 5);
        } else {
            // 胜者分数高于败者，获得较少分数
            // 每相差100分，减少5分，最少10分
            transferPoints = Math.max(10, basePoints + (ratingDiff / 100) * 5);
        }
        
        // 更新分数
        winner.setMatchRating(winnerRating + transferPoints);
        winner.setUpdatedAt(OffsetDateTime.now());
        userAccountMapper.updateById(winner);
        
        loser.setMatchRating(Math.max(0, loserRating - transferPoints));
        loser.setUpdatedAt(OffsetDateTime.now());
        userAccountMapper.updateById(loser);
        
        log.info("常规匹配结算: 胜者 {} ({} -> {}), 败者 {} ({} -> {})", 
            winnerId, winnerRating, winner.getMatchRating(),
            loserId, loserRating, loser.getMatchRating());
        
        return new int[]{transferPoints, -transferPoints};
    }
}
