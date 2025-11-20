package org.example.card.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡牌数据验证器 - 检查卡牌配置是否合法
 */
public class CardDataValidator {
    
    private static final Logger log = LoggerFactory.getLogger(CardDataValidator.class);
    
    /**
     * 验证卡牌数据
     */
    public static ValidationResult validate(CardData data) {
        ValidationResult result = new ValidationResult();
        
        // 基础字段验证
        if (data.getId() == null || data.getId().isEmpty()) {
            result.addError("卡牌ID不能为空");
        }
        
        if (data.getName() == null || data.getName().isEmpty()) {
            result.addError("卡牌名称不能为空");
        }
        
        if (data.getCardType() == null || data.getCardType().isEmpty()) {
            result.addError("卡牌类型不能为空");
        } else {
            // 验证卡牌类型是否合法
            String type = data.getCardType().toUpperCase();
            if (!type.equals("FOLLOW") && !type.equals("SPELL") && 
                !type.equals("AMULET") && !type.equals("EQUIP")) {
                result.addError("卡牌类型必须是: FOLLOW, SPELL, AMULET, EQUIP 之一");
            }
        }
        
        if (data.getCost() < 0) {
            result.addError("费用不能为负数");
        }
        
        if (data.getCost() > 20) {
            result.addWarning("费用过高: " + data.getCost());
        }
        
        // 验证稀有度
        if (data.getRarity() != null) {
            String rarity = data.getRarity().toUpperCase();
            if (!rarity.equals("BRONZE") && !rarity.equals("SILVER") && 
                !rarity.equals("GOLD") && !rarity.equals("RAINBOW") && 
                !rarity.equals("LEGENDARY")) {
                result.addError("稀有度必须是: BRONZE, SILVER, GOLD, RAINBOW, LEGENDARY 之一");
            }
        }
        
        // 根据卡牌类型验证特定字段
        String cardType = data.getCardType() != null ? data.getCardType().toUpperCase() : "";
        
        switch (cardType) {
            case "FOLLOW":
                validateFollowCard(data, result);
                break;
            case "SPELL":
                validateSpellCard(data, result);
                break;
            case "AMULET":
                validateAmuletCard(data, result);
                break;
            case "EQUIP":
                validateEquipCard(data, result);
                break;
        }
        
        // 验证效果
        if (data.getEffects() != null) {
            for (int i = 0; i < data.getEffects().size(); i++) {
                validateEffect(data.getEffects().get(i), i, result);
            }
        }
        
        return result;
    }
    
    private static void validateFollowCard(CardData data, ValidationResult result) {
        if (data.getAtk() == null) {
            result.addError("随从卡必须指定攻击力(atk)");
        } else if (data.getAtk() < 0) {
            result.addError("攻击力不能为负数");
        } else if (data.getAtk() > 99) {
            result.addWarning("攻击力过高: " + data.getAtk());
        }
        
        if (data.getHp() == null) {
            result.addError("随从卡必须指定生命值(hp)");
        } else if (data.getHp() <= 0) {
            result.addError("生命值必须大于0");
        } else if (data.getHp() > 99) {
            result.addWarning("生命值过高: " + data.getHp());
        }
    }
    
    private static void validateSpellCard(CardData data, ValidationResult result) {
        if (data.getEffects() == null || data.getEffects().isEmpty()) {
            result.addWarning("法术卡没有效果");
        }
    }
    
    private static void validateAmuletCard(CardData data, ValidationResult result) {
        if (data.getCountdown() == null) {
            result.addWarning("护符卡未指定倒数值，将使用默认值-1");
        }
        
        if (data.getEffects() == null || data.getEffects().isEmpty()) {
            result.addWarning("护符卡没有效果");
        }
    }
    
    private static void validateEquipCard(CardData data, ValidationResult result) {
        if (data.getAddAtk() == null) {
            result.addWarning("装备卡未指定攻击力加成(addAtk)");
        } else if (data.getAddAtk() < 0) {
            result.addError("攻击力加成不能为负数");
        }
        
        if (data.getDurability() == null) {
            result.addWarning("装备卡未指定耐久度(durability)");
        } else if (data.getDurability() <= 0) {
            result.addError("耐久度必须大于0");
        }
    }
    
    private static void validateEffect(CardData.EffectData effect, int index, ValidationResult result) {
        String prefix = "效果[" + index + "] ";
        
        if (effect.getTiming() == null || effect.getTiming().isEmpty()) {
            result.addError(prefix + "必须指定触发时机(timing)");
        }
        
        if (effect.getScript() == null || effect.getScript().isEmpty()) {
            // 如果没有脚本，必须有效果类型
            if (effect.getType() == null || effect.getType().isEmpty()) {
                result.addError(prefix + "必须指定效果类型(type)或脚本(script)");
            }
        }
        
        // 验证参数
        if (effect.getParams() != null) {
            validateEffectParams(effect, prefix, result);
        }
    }
    
    private static void validateEffectParams(CardData.EffectData effect, String prefix, ValidationResult result) {
        CardData.EffectParams params = effect.getParams();
        String type = effect.getType();
        
        if (type == null) return;
        
        switch (type.toUpperCase()) {
            case "DRAW_CARD":
                if (params.getAmount() == null || params.getAmount() <= 0) {
                    result.addWarning(prefix + "抽牌效果建议指定数量(amount)");
                }
                break;
            case "DEAL_DAMAGE":
            case "HEAL":
                if (params.getAmount() == null || params.getAmount() <= 0) {
                    result.addWarning(prefix + "伤害/治疗效果建议指定数量(amount)");
                }
                if (effect.getTarget() == null || effect.getTarget().isEmpty()) {
                    result.addError(prefix + "伤害/治疗效果必须指定目标(target)");
                }
                break;
            case "BUFF":
                if ((params.getBuffAtk() == null || params.getBuffAtk() == 0) && 
                    (params.getBuffHp() == null || params.getBuffHp() == 0)) {
                    result.addWarning(prefix + "增益效果建议指定攻击力或生命值加成");
                }
                break;
            case "SUMMON":
                if (params.getSummonCard() == null || params.getSummonCard().isEmpty()) {
                    result.addError(prefix + "召唤效果必须指定召唤的卡牌ID(summonCard)");
                }
                break;
        }
    }
    
    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public void logResults(String cardId) {
            if (!isValid()) {
                log.error("卡牌 {} 验证失败:", cardId);
                errors.forEach(e -> log.error("  - {}", e));
            }
            
            if (!warnings.isEmpty()) {
                log.warn("卡牌 {} 有警告:", cardId);
                warnings.forEach(w -> log.warn("  - {}", w));
            }
            
            if (isValid() && warnings.isEmpty()) {
                log.debug("卡牌 {} 验证通过", cardId);
            }
        }
    }
}
