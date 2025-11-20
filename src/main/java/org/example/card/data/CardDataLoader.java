package org.example.card.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.card.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 卡牌数据加载器 - 从JSON文件加载卡牌配置
 */
@Component
public class CardDataLoader {
    
    private static final Logger log = LoggerFactory.getLogger(CardDataLoader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, CardData> cardDataCache = new HashMap<>();
    private static final Map<String, Card> dataCardPrototypes = new HashMap<>();
    
    /**
     * 加载所有JSON卡牌配置文件
     */
    public static void loadAllCardData() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:cards/**/*.json");
            
            log.info("开始加载卡牌数据文件，共 {} 个文件", resources.length);
            
            for (Resource resource : resources) {
                try {
                    CardData cardData = objectMapper.readValue(resource.getInputStream(), CardData.class);
                    
                    // 验证卡牌数据
                    CardDataValidator.ValidationResult validation = CardDataValidator.validate(cardData);
                    validation.logResults(cardData.getId());
                    
                    if (!validation.isValid()) {
                        log.error("跳过无效卡牌: {}", resource.getFilename());
                        continue;
                    }
                    
                    cardDataCache.put(cardData.getId(), cardData);
                    
                    // 创建并缓存卡牌原型
                    Card card = DataDrivenCard.createCard(cardData);
                    card.init();
                    dataCardPrototypes.put(cardData.getId(), card);
                    
                    log.debug("加载卡牌: {} ({})", cardData.getName(), cardData.getId());
                } catch (Exception e) {
                    log.error("加载卡牌文件失败: {} - 错误: {}", resource.getFilename(), e.getMessage());
                    log.debug("详细堆栈: ", e);
                }
            }
            
            log.info("卡牌数据加载完成，共 {} 张数据驱动卡牌", cardDataCache.size());
            
        } catch (IOException e) {
            log.error("加载卡牌数据文件失败", e);
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
}
