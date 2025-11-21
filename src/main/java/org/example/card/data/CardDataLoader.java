package org.example.card.data;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.card.Card;
import org.example.card.data.entity.CardDataEntity;
import org.example.card.data.mapper.CardDataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 卡牌数据加载器 - 从数据库加载卡牌配置
 */
@Component
public class CardDataLoader {
    
    private static final Logger log = LoggerFactory.getLogger(CardDataLoader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, CardData> cardDataCache = new ConcurrentHashMap<>();
    private static final Map<String, Card> dataCardPrototypes = new ConcurrentHashMap<>();
    
    private static CardDataMapper cardDataMapper;

    @Autowired
    public void setCardDataMapper(CardDataMapper cardDataMapper) {
        CardDataLoader.cardDataMapper = cardDataMapper;
    }
    
    /**
     * 定时刷新卡牌数据（每分钟）
     */
    @Scheduled(fixedRate = 60000)
    public void autoReload() {
        log.debug("执行卡牌数据自动刷新...");
        loadAllCardData();
    }

    /**
     * 加载所有卡牌数据
     */
    public static synchronized void loadAllCardData() {
        // 清理旧数据
        cardDataCache.clear();
        dataCardPrototypes.clear();
        
        loadFromDatabase();
        
        log.info("卡牌数据加载完成，共 {} 张数据驱动卡牌", cardDataCache.size());
    }

    private static void loadFromDatabase() {
        if (cardDataMapper == null) {
            log.warn("CardDataMapper未注入，跳过数据库加载");
            return;
        }
        try {
            List<CardDataEntity> entities = cardDataMapper.selectList(
                new LambdaQueryWrapper<CardDataEntity>().eq(CardDataEntity::getEnabled, true)
            );
            log.info("开始加载数据库卡牌数据，共 {} 条记录", entities.size());
            
            for (CardDataEntity entity : entities) {
                try {
                    CardData cardData = objectMapper.readValue(entity.getDataJson(), CardData.class);
                    // 确保ID一致
                    if (!entity.getId().equals(cardData.getId())) {
                        cardData.setId(entity.getId());
                    }
                    registerCard(cardData, "DB: " + entity.getId());
                } catch (Exception e) {
                    log.error("解析数据库卡牌数据失败: {} - 错误: {}", entity.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("加载数据库卡牌失败", e);
        }
    }

    private static void registerCard(CardData cardData, String source) {
        // 验证卡牌数据
        CardDataValidator.ValidationResult validation = CardDataValidator.validate(cardData);
        if (!validation.isValid()) {
            log.error("跳过无效卡牌 [{}]: {}", source, cardData.getId());
            validation.logResults(cardData.getId());
            return;
        }
        
        cardDataCache.put(cardData.getId(), cardData);
        
        // 创建并缓存卡牌原型
        try {
            Card card = DataDrivenCard.createCard(cardData);
            card.init();
            dataCardPrototypes.put(cardData.getId(), card);
            log.debug("加载卡牌成功: {} ({}) from {}", cardData.getName(), cardData.getId(), source);
        } catch (Exception e) {
            log.error("创建卡牌原型失败 [{}]: {}", source, e.getMessage());
        }
    }
    
    /**
     * 根据卡牌ID获取CardData
     */
    public static CardData getCardData(String cardId) {
        return cardDataCache.get(cardId);
    }
    
    /**
     * 根据卡牌ID创建新的卡牌实例
     */
    public static Card createCardById(String cardId) {
        Card prototype = dataCardPrototypes.get(cardId);
        if (prototype == null) {
            log.warn("未找到卡牌数据: {}", cardId);
            return null;
        }
        Card card = prototype.prototype();
        card.init(); // 初始化卡牌字段
        return card;
    }
    
    /**
     * 根据卡牌名称获取原型
     */
    public static Card getPrototypeByName(String cardName) {
        return dataCardPrototypes.values().stream()
            .filter(card -> card.getName().equals(cardName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 获取所有数据驱动的卡牌原型
     */
    public static Map<String, Card> getAllDataCardPrototypes() {
        return new HashMap<>(dataCardPrototypes);
    }
    
    /**
     * 检查是否是数据驱动的卡牌
     */
    public static boolean isDataDrivenCard(String cardName) {
        return dataCardPrototypes.values().stream()
            .anyMatch(card -> card.getName().equals(cardName));
    }

    /**
     * 重新加载数据
     */
    public static void reload() {
        loadAllCardData();
    }
}
