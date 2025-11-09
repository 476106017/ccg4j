package org.example.constant;

/**
 * 卡牌稀有度枚举
 * 从低到高分为5个等级
 */
public enum CardRarity {
    BRONZE("铜卡", 1),
    SILVER("银卡", 2),
    GOLD("金卡", 3),
    RAINBOW("彩虹卡", 4),
    LEGENDARY("传说卡", 5);

    private final String displayName;
    private final int level;

    CardRarity(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String displayName() {
        return displayName;
    }

    public int level() {
        return level;
    }
}
