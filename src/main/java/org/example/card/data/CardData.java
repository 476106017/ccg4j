package org.example.card.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.example.constant.CardRarity;
import org.example.constant.CardType;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡牌数据配置类 - 用于从JSON加载卡牌
 */
@Data
public class CardData {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("cardType")
    private String cardType; // FOLLOW, SPELL, AMULET, EQUIP
    
    @JsonProperty("cost")
    private int cost = 0;
    
    @JsonProperty("rarity")
    private String rarity = "BRONZE";
    
    @JsonProperty("race")
    private List<String> race = new ArrayList<>();
    
    @JsonProperty("job")
    private String job = "";
    
    @JsonProperty("keywords")
    private List<String> keywords = new ArrayList<>();
    
    @JsonProperty("description")
    private String description = "";
    
    @JsonProperty("subMark")
    private String subMark = "";
    
    // 随从卡属性
    @JsonProperty("atk")
    private Integer atk;
    
    @JsonProperty("hp")
    private Integer hp;
    
    // 护符卡属性
    @JsonProperty("countdown")
    private Integer countdown;
    
    // 装备卡属性
    @JsonProperty("addAtk")
    private Integer addAtk;
    
    @JsonProperty("durability")
    private Integer durability;
    
    // 效果配置
    @JsonProperty("effects")
    private List<EffectData> effects = new ArrayList<>();
    
    @Data
    public static class EffectData {
        @JsonProperty("timing")
        private String timing; // BATTLECRY, DEATHRATTLE, TURN_START, etc.
        
        @JsonProperty("type")
        private String type; // DRAW_CARD, DEAL_DAMAGE, SUMMON, etc.
        
        @JsonProperty("target")
        private String target; // SELF, ENEMY_LEADER, etc.
        
        @JsonProperty("params")
        private EffectParams params = new EffectParams();
        
        @JsonProperty("condition")
        private String condition; // 触发条件
        
        @JsonProperty("script")
        private String script; // 自定义脚本名称
    }
    
    @Data
    public static class EffectParams {
        @JsonProperty("amount")
        private Integer amount;
        
        @JsonProperty("cardFilter")
        private String cardFilter;
        
        @JsonProperty("summonCard")
        private String summonCard;
        
        @JsonProperty("buffAtk")
        private Integer buffAtk;
        
        @JsonProperty("buffHp")
        private Integer buffHp;
        
        @JsonProperty("keywords")
        private List<String> keywords;
    }
}
