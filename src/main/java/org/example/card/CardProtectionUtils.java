package org.example.card;

import org.example.game.PlayerInfo;

/**
 * 卡牌操作安全检查工具类
 */
public class CardProtectionUtils {

    /**
     * 检查卡牌是否已绑定 GameInfo
     * @param card 要检查的卡牌
     * @param operation 要执行的操作名称
     * @throws IllegalStateException 如果卡牌未绑定 GameInfo
     */
    public static void checkInfoBound(Card card, String operation) {
        if (card.getInfo() == null) {
            throw new IllegalStateException(
                String.format("Cannot perform %s on unbound card prototype %s",
                    operation, card.getClass().getSimpleName()));
        }
    }

    /**
     * 安全地复制卡牌到指定玩家
     * @param card 要复制的卡牌
     * @param player 目标玩家
     * @return 复制后的卡牌实例
     */
    public static Card safeCopyCard(Card card, PlayerInfo player) {
        if (player == null) {
            throw new IllegalArgumentException("Target player cannot be null");
        }
        return card.copyBy(player);
    }
}
