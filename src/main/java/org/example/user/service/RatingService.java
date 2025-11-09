package org.example.user.service;

public interface RatingService {
    /**
     * 获取用户当前分数
     */
    Integer getRating(Long userId);
    
    /**
     * 对战结算分数
     * @param winnerId 胜者ID
     * @param loserId 败者ID
     * @return 分数变化 [胜者变化, 败者变化]
     */
    int[] settleBattle(Long winnerId, Long loserId);
}
