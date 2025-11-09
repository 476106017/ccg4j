package org.example.card.dto;

import lombok.Builder;
import lombok.Value;
import org.example.constant.CardRarity;

import java.util.List;

@Value
@Builder
public class CardSummary {
    String code;
    String name;
    String type;
    Integer cost;
    String job;
    List<String> race;
    List<String> keywords;
    String mark;
    Integer atk;
    Integer hp;
    Integer countdown;  // 护符卡初始倒数，装备卡耐久度
    Integer addAtk;     // 装备卡攻击力
    CardRarity rarity;
}
