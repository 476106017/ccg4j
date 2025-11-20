package org.example.card.data;

import org.example.card.Card;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 效果构建器 - 根据配置数据构建卡牌效果
 */
public class EffectBuilder {
    
    private static final Logger log = LoggerFactory.getLogger(EffectBuilder.class);
    
    // 效果类型映射
    private static final Map<String, BiFunction<Card, CardData.EffectData, Effect>> EFFECT_BUILDERS = new HashMap<>();
    
    static {
        // 注册基础效果构建器
        EFFECT_BUILDERS.put("DRAW_CARD", EffectBuilder::buildDrawCardEffect);
        EFFECT_BUILDERS.put("DEAL_DAMAGE", EffectBuilder::buildDealDamageEffect);
        EFFECT_BUILDERS.put("HEAL", EffectBuilder::buildHealEffect);
        EFFECT_BUILDERS.put("BUFF", EffectBuilder::buildBuffEffect);
        EFFECT_BUILDERS.put("SUMMON", EffectBuilder::buildSummonEffect);
        EFFECT_BUILDERS.put("ADD_KEYWORD", EffectBuilder::buildAddKeywordEffect);
        EFFECT_BUILDERS.put("DISCOVER", EffectBuilder::buildDiscoverEffect);
    }
    
    /**
     * 根据效果数据构建效果实例
     */
    public static Effect buildEffect(Card card, CardData.EffectData effectData) {
        // 如果指定了自定义脚本，尝试加载脚本
        if (effectData.getScript() != null && !effectData.getScript().isEmpty()) {
            Effect scriptEffect = ScriptEffectLoader.loadEffect(effectData.getScript(), card, effectData);
            if (scriptEffect != null) {
                return scriptEffect;
            }
        }
        
        // 使用预定义的效果构建器
        String type = effectData.getType();
        BiFunction<Card, CardData.EffectData, Effect> builder = EFFECT_BUILDERS.get(type);
        
        if (builder == null) {
            log.warn("Unknown effect type: {} for card: {}", type, card.getName());
            return null;
        }
        
        return builder.apply(card, effectData);
    }
    
    /**
     * 构建抽牌效果
     */
    private static Effect buildDrawCardEffect(Card card, CardData.EffectData data) {
        int amount = data.getParams().getAmount() != null ? data.getParams().getAmount() : 1;
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming()), obj -> {
            card.ownerPlayer().draw(amount);
        });
    }
    
    /**
     * 构建造成伤害效果 - 待实现
     */
    private static Effect buildDealDamageEffect(Card card, CardData.EffectData data) {
        // TODO: 需要实现damage方法
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming()), obj -> {
            log.warn("DEAL_DAMAGE effect not yet implemented for card: {}", card.getName());
        });
    }
    
    /**
     * 构建治疗效果 - 待实现
     */
    private static Effect buildHealEffect(Card card, CardData.EffectData data) {
        // TODO: 需要实现heal方法
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming()), obj -> {
            log.warn("HEAL effect not yet implemented for card: {}", card.getName());
        });
    }
    
    /**
     * 构建增益效果
     */
    private static Effect buildBuffEffect(Card card, CardData.EffectData data) {
        int buffAtk = data.getParams().getBuffAtk() != null ? data.getParams().getBuffAtk() : 0;
        int buffHp = data.getParams().getBuffHp() != null ? data.getParams().getBuffHp() : 0;
        String target = data.getTarget();
        
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming().toUpperCase()), obj -> {
            GameObj buffTarget = resolveTarget(card, target);
            if (buffTarget instanceof org.example.card.FollowCard) {
                org.example.card.FollowCard follow = (org.example.card.FollowCard) buffTarget;
                follow.addStatus(buffAtk, buffHp);
            }
        });
    }
    
    /**
     * 构建召唤效果 - 待实现
     */
    private static Effect buildSummonEffect(Card card, CardData.EffectData data) {
        // TODO: 需要实现summon方法
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming()), obj -> {
            log.warn("SUMMON effect not yet implemented for card: {}", card.getName());
        });
    }
    
    /**
     * 构建添加关键词效果
     */
    private static Effect buildAddKeywordEffect(Card card, CardData.EffectData data) {
        var keywords = data.getParams().getKeywords();
        String target = data.getTarget();
        
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming().toUpperCase()), obj -> {
            GameObj keywordTarget = resolveTarget(card, target);
            if (keywordTarget instanceof Card) {
                Card targetCard = (Card) keywordTarget;
                if (keywords != null) {
                    targetCard.addKeywords(keywords);
                }
            }
        });
    }
    
    /**
     * 构建发现效果 - 待实现
     */
    private static Effect buildDiscoverEffect(Card card, CardData.EffectData data) {
        // TODO: 需要实现discover方法
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming()), obj -> {
            log.warn("DISCOVER effect not yet implemented for card: {}", card.getName());
        });
    }
    
    /**
     * 解析目标
     */
    @SuppressWarnings("unused")
    private static GameObj resolveTarget(Card sourceCard, String targetType) {
        if (targetType == null) return null;
        
        switch (targetType.toUpperCase()) {
            case "SELF":
                return sourceCard;
            case "ENEMY_LEADER":
                return sourceCard.enemyLeader();
            case "MY_LEADER":
                return sourceCard.ownerLeader();
            default:
                return null;
        }
    }
}
