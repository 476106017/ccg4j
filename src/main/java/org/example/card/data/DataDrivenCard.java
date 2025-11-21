package org.example.card.data;

import org.example.card.*;
import org.example.constant.CardRarity;
import org.example.constant.CardType;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据驱动的卡牌实现 - 根据CardData动态生成卡牌行为
 */
public class DataDrivenCard {
    
    /**
     * 从CardData创建对应类型的卡牌实例
     */
    public static Card createCard(CardData data) {
        Card card;
        
        switch (data.getCardType().toUpperCase()) {
            case "FOLLOW":
                card = new DataFollowCard(data);
                break;
            case "SPELL":
                card = new DataSpellCard(data);
                break;
            case "AMULET":
                card = new DataAmuletCard(data);
                break;
            case "EQUIP":
                card = new DataEquipmentCard(data);
                break;
            default:
                throw new IllegalArgumentException("Unknown card type: " + data.getCardType());
        }
        
        return card;
    }
    
    /**
     * 数据驱动的随从卡
     */
    public static class DataFollowCard extends FollowCard {
        private final CardData data;
        public String name = "";
        public Integer cost = 0;
        public CardRarity rarity = CardRarity.BRONZE;
        public String job = "";
        public List<String> race = new ArrayList<>();
        public String mark = "";
        public String subMark = "";
        
        public DataFollowCard(CardData data) {
            this.data = data;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public Integer getCost() {
            return cost;
        }
        
        @Override
        public void setCost(Integer cost) {
            this.cost = cost;
        }
        
        @Override
        public CardRarity getRarity() {
            return rarity;
        }
        
        @Override
        public void setRarity(CardRarity rarity) {
            this.rarity = rarity;
        }
        
        @Override
        public String getJob() {
            return job;
        }
        
        @Override
        public List<String> getRace() {
            return race;
        }
        
        @Override
        public String getSubMark() {
            return subMark;
        }
        
        @Override
        public String getMark() {
            return mark;
        }
        
        @Override
        public void init() {
            setName(data.getName());
            setCost(data.getCost());
            setAtk(data.getAtk() != null ? data.getAtk() : 0);
            setHp(data.getHp() != null ? data.getHp() : 0);
            setMaxHp(getHp());
            setRarity(CardRarity.valueOf(data.getRarity()));
            getRace().addAll(data.getRace());
            this.job = data.getJob();
            this.mark = data.getDescription() != null ? data.getDescription() : "";
            this.subMark = data.getSubMark() != null ? data.getSubMark() : "";
            getKeywords().addAll(data.getKeywords());
            
            // 加载效果
            loadEffects();
        }
        
        private void loadEffects() {
            for (CardData.EffectData effectData : data.getEffects()) {
                Effect effect = EffectBuilder.buildEffect(this, effectData);
                if (effect != null) {
                    addEffects(effect); // 使用addEffects而不是addEffect
                }
            }
        }
        
        @Override
        public Card prototype() {
            return new DataFollowCard(data);
        }

        @Override
        public Card createInstance() {
            return new DataFollowCard(data);
        }
    }
    
    /**
     * 数据驱动的法术卡
     */
    public static class DataSpellCard extends SpellCard {
        private final CardData data;
        public String name = "";
        public Integer cost = 0;
        public CardRarity rarity = CardRarity.BRONZE;
        public String job = "";
        public List<String> race = new ArrayList<>();
        public String mark = "";
        public String subMark = "";
        
        public DataSpellCard(CardData data) {
            this.data = data;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public Integer getCost() {
            return cost;
        }
        
        @Override
        public void setCost(Integer cost) {
            this.cost = cost;
        }
        
        @Override
        public CardRarity getRarity() {
            return rarity;
        }
        
        @Override
        public void setRarity(CardRarity rarity) {
            this.rarity = rarity;
        }
        
        @Override
        public String getJob() {
            return job;
        }
        
        @Override
        public List<String> getRace() {
            return race;
        }
        
        @Override
        public String getSubMark() {
            return subMark;
        }
        
        @Override
        public String getMark() {
            return mark;
        }
        
        @Override
        public void init() {
            setName(data.getName());
            setCost(data.getCost());
            setRarity(CardRarity.valueOf(data.getRarity()));
            getRace().addAll(data.getRace());
            this.job = data.getJob();
            this.mark = data.getDescription() != null ? data.getDescription() : "";
            this.subMark = data.getSubMark() != null ? data.getSubMark() : "";
            getKeywords().addAll(data.getKeywords());
            
            loadEffects();
        }
        
        private void loadEffects() {
            for (CardData.EffectData effectData : data.getEffects()) {
                Effect effect = EffectBuilder.buildEffect(this, effectData);
                if (effect != null) {
                    addEffects(effect);
                }
            }
        }
        
        @Override
        public Card prototype() {
            return new DataSpellCard(data);
        }

        @Override
        public Card createInstance() {
            return new DataSpellCard(data);
        }
    }
    
    /**
     * 数据驱动的护符卡
     */
    public static class DataAmuletCard extends AmuletCard {
        private final CardData data;
        public String name = "";
        public Integer cost = 0;
        public CardRarity rarity = CardRarity.BRONZE;
        public String job = "";
        public List<String> race = new ArrayList<>();
        public String mark = "";
        public String subMark = "";
        
        public DataAmuletCard(CardData data) {
            this.data = data;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public Integer getCost() {
            return cost;
        }
        
        @Override
        public void setCost(Integer cost) {
            this.cost = cost;
        }
        
        @Override
        public CardRarity getRarity() {
            return rarity;
        }
        
        @Override
        public void setRarity(CardRarity rarity) {
            this.rarity = rarity;
        }
        
        @Override
        public String getJob() {
            return job;
        }
        
        @Override
        public List<String> getRace() {
            return race;
        }
        
        @Override
        public String getSubMark() {
            return subMark;
        }
        
        @Override
        public String getMark() {
            return mark;
        }
        
        @Override
        public void init() {
            setName(data.getName());
            setCost(data.getCost());
            setCountDown(data.getCountdown() != null ? data.getCountdown() : -1);
            setRarity(CardRarity.valueOf(data.getRarity()));
            getRace().addAll(data.getRace());
            this.job = data.getJob();
            this.mark = data.getDescription() != null ? data.getDescription() : "";
            this.subMark = data.getSubMark() != null ? data.getSubMark() : "";
            getKeywords().addAll(data.getKeywords());
            
            loadEffects();
        }
        
        private void loadEffects() {
            for (CardData.EffectData effectData : data.getEffects()) {
                Effect effect = EffectBuilder.buildEffect(this, effectData);
                if (effect != null) {
                    addEffects(effect);
                }
            }
        }
        
        @Override
        public Card prototype() {
            return new DataAmuletCard(data);
        }

        @Override
        public Card createInstance() {
            return new DataAmuletCard(data);
        }
    }
    
    /**
     * 数据驱动的装备卡
     */
    public static class DataEquipmentCard extends EquipmentCard {
        private final CardData data;
        public String name = "";
        public Integer cost = 0;
        public CardRarity rarity = CardRarity.BRONZE;
        public String job = "";
        public List<String> race = new ArrayList<>();
        public String mark = "";
        public String subMark = "";
        
        public DataEquipmentCard(CardData data) {
            this.data = data;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public Integer getCost() {
            return cost;
        }
        
        @Override
        public void setCost(Integer cost) {
            this.cost = cost;
        }
        
        @Override
        public CardRarity getRarity() {
            return rarity;
        }
        
        @Override
        public void setRarity(CardRarity rarity) {
            this.rarity = rarity;
        }
        
        @Override
        public String getJob() {
            return job;
        }
        
        @Override
        public List<String> getRace() {
            return race;
        }
        
        @Override
        public String getSubMark() {
            return subMark;
        }
        
        @Override
        public String getMark() {
            return mark;
        }
        
        @Override
        public void init() {
            setName(data.getName());
            setCost(data.getCost());
            setAddAtk(data.getAddAtk() != null ? data.getAddAtk() : 0);
            setCountdown(data.getDurability() != null ? data.getDurability() : 2);
            setRarity(CardRarity.valueOf(data.getRarity()));
            getRace().addAll(data.getRace());
            this.job = data.getJob();
            this.mark = data.getDescription() != null ? data.getDescription() : "";
            this.subMark = data.getSubMark() != null ? data.getSubMark() : "";
            getKeywords().addAll(data.getKeywords());
            
            loadEffects();
        }
        
        private void loadEffects() {
            for (CardData.EffectData effectData : data.getEffects()) {
                Effect effect = EffectBuilder.buildEffect(this, effectData);
                if (effect != null) {
                    addEffects(effect);
                }
            }
        }
        
        @Override
        public Card prototype() {
            return new DataEquipmentCard(data);
        }

        @Override
        public Card createInstance() {
            return new DataEquipmentCard(data);
        }
    }
    
}
