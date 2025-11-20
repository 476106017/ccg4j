package org.example.card.data;

import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 脚本效果加载器 - 用于加载自定义的复杂效果
 * 
 * 可以扩展为真正的脚本引擎（如JavaScript、Groovy等）
 * 目前使用Java实现的预定义脚本
 */
public class ScriptEffectLoader {
    
    private static final Logger log = LoggerFactory.getLogger(ScriptEffectLoader.class);
    
    // 预定义脚本映射
    private static final Map<String, BiFunction<Card, CardData.EffectData, Effect>> SCRIPT_EFFECTS = new HashMap<>();
    
    static {
        // 注册示例脚本
        registerScript("draw_if_dragon", ScriptEffectLoader::drawIfDragonScript);
        registerScript("buff_if_dragon", ScriptEffectLoader::buffIfDragonScript);
        registerScript("summon_copy", ScriptEffectLoader::summonCopyScript);
        registerScript("buff_all_allies", ScriptEffectLoader::buffAllAlliesScript);
    }
    
    /**
     * 注册自定义脚本
     */
    public static void registerScript(String name, BiFunction<Card, CardData.EffectData, Effect> scriptFunction) {
        SCRIPT_EFFECTS.put(name, scriptFunction);
        log.debug("注册脚本效果: {}", name);
    }
    
    /**
     * 加载脚本效果
     */
    public static Effect loadEffect(String scriptName, Card card, CardData.EffectData effectData) {
        BiFunction<Card, CardData.EffectData, Effect> scriptFunction = SCRIPT_EFFECTS.get(scriptName);
        
        if (scriptFunction == null) {
            log.warn("未找到脚本: {} for card: {}", scriptName, card.getName());
            return null;
        }
        
        try {
            return scriptFunction.apply(card, effectData);
        } catch (Exception e) {
            log.error("执行脚本失败: {} for card: {}", scriptName, card.getName(), e);
            return null;
        }
    }
    
    // ==================== 示例脚本实现 ====================
    
    /**
     * 示例脚本：如果我方场上有龙族随从则抽牌
     */
    private static Effect drawIfDragonScript(Card card, CardData.EffectData data) {
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming()), obj -> {
            boolean hasDragon = card.ownerPlayer().getArea().stream()
                .anyMatch(c -> c.hasRace("龙"));
            if (hasDragon) {
                card.ownerPlayer().draw(1);
            }
        });
    }
    
    /**
     * 示例脚本：如果我方场上有龙族随从则获得增益
     */
    private static Effect buffIfDragonScript(Card card, CardData.EffectData data) {
        int buffAtk = data.getParams().getBuffAtk() != null ? data.getParams().getBuffAtk() : 2;
        int buffHp = data.getParams().getBuffHp() != null ? data.getParams().getBuffHp() : 2;
        
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming()), obj -> {
            boolean hasDragon = card.ownerPlayer().getArea().stream()
                .anyMatch(c -> c.hasRace("龙"));
            if (hasDragon && card instanceof FollowCard) {
                ((FollowCard) card).addStatus(buffAtk, buffHp);
            }
        });
    }
    
    /**
     * 示例脚本：召唤自身的复制
     */
    private static Effect summonCopyScript(Card card, CardData.EffectData data) {
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming()), obj -> {
            Card copy = card.prototype();
            copy.init(); // 初始化复制的卡牌
            if (copy instanceof org.example.card.AreaCard) {
                card.ownerPlayer().summon((org.example.card.AreaCard)copy);
            }
        });
    }
    
    /**
     * 示例脚本：为所有我方随从提供增益
     */
    private static Effect buffAllAlliesScript(Card card, CardData.EffectData data) {
        int buffAtk = data.getParams().getBuffAtk() != null ? data.getParams().getBuffAtk() : 1;
        int buffHp = data.getParams().getBuffHp() != null ? data.getParams().getBuffHp() : 1;
        
        return new Effect(card, card, EffectTiming.valueOf(data.getTiming()), obj -> {
            card.ownerPlayer().getArea().forEach(c -> {
                if (c instanceof FollowCard) {
                    ((FollowCard) c).addStatus(buffAtk, buffHp);
                }
            });
        });
    }
}
