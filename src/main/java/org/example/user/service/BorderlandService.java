package org.example.user.service;

import org.example.user.entity.BorderlandVisa;

import java.util.List;

public interface BorderlandService {
    /**
     * 获取用户当前签证状态
     */
    BorderlandVisa getVisaStatus(Long userId);
    
    /**
     * 办理新签证
     */
    BorderlandVisa applyVisa(Long userId);
    
    /**
     * 丢弃一张卡牌
     */
    BorderlandVisa discardCard(Long userId, String cardCode);
    
    /**
     * 对战结算 - 胜者获取败者的卡牌和天数
     */
    void settleBattle(Long winnerId, Long loserId, boolean isAchievement);
    
    /**
     * 击败AI后增加签证天数
     */
    void winAgainstAI(Long userId);
    
    /**
     * 击败AI后获得AI的卡组
     * @return 获得的卡牌代码
     */
    String winAgainstAI(Long userId, List<String> aiDeckCards);
    
    /**
     * 输给AI后进入惩罚期
     */
    void loseAgainstAI(Long userId);
    
    /**
     * 带出一张卡牌到收藏
     */
    void exportCard(Long userId, String cardCode);
}
