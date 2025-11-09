package org.example.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.system.mapper.ConfigDictMapper;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏配置（从数据库字典表加载）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GameConfig {
    
    private final ConfigDictMapper configDictMapper;
    
    // 配置缓存
    private final ConcurrentHashMap<String, String> configCache = new ConcurrentHashMap<>();
    
    /**
     * 初始化时加载配置
     */
    @PostConstruct
    public void init() {
        reloadConfig();
        log.info("游戏配置已加载: AI等待{}秒, 回合超时{}秒, 短引线{}秒", 
            getAiMatchWaitSeconds(), getTurnTimeoutSeconds(), getShortRopeSeconds());
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        configCache.clear();
        // 这里可以一次性加载所有配置，或者按需加载
    }
    
    /**
     * 获取整型配置
     */
    private int getIntConfig(String key, int defaultValue) {
        try {
            String value = configCache.computeIfAbsent(key, k -> {
                String v = configDictMapper.getValueByKey(k);
                return v != null ? v : String.valueOf(defaultValue);
            });
            return Integer.parseInt(value);
        } catch (Exception e) {
            log.warn("获取配置{}失败，使用默认值{}", key, defaultValue, e);
            return defaultValue;
        }
    }
    
    /**
     * AI搜寻等待时间（秒）
     */
    public int getAiMatchWaitSeconds() {
        return getIntConfig("game.ai_match_wait_seconds", 10);
    }
    
    /**
     * 正常回合超时时间（秒）
     */
    public int getTurnTimeoutSeconds() {
        return getIntConfig("game.turn_timeout_seconds", 300);
    }
    
    /**
     * 短引线超时时间（秒）
     */
    public int getShortRopeSeconds() {
        return getIntConfig("game.short_rope_seconds", 30);
    }
}
