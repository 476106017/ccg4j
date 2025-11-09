package org.example.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置字典表
 */
@Data
@TableName("config_dict")
public class ConfigDict {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 配置键，唯一标识
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置类型：string, int, boolean, json
     */
    private String configType;
    
    /**
     * 配置分组：game, system, match等
     */
    private String configGroup;
    
    /**
     * 配置说明
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
