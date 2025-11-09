package org.example.card.dto;

import lombok.Builder;
import lombok.Value;
import org.example.constant.CardRarity;

import java.util.List;

/**
 * 前端卡牌展示DTO
 */
@Value
@Builder
public class CardDisplayDto {
    String code;
    String name;
    String cardType;  // 前端使用 cardType
    Integer cost;
    String job;
    List<String> race;
    String mark;
    Integer attack;   // 前端使用 attack
    Integer health;   // 前端使用 health
    Integer countdown; // 护符卡初始倒数，装备卡耐久度
    Integer addAtk;   // 装备卡攻击力
    String description;
    List<String> keywords;
    CardRarity rarity;
}
