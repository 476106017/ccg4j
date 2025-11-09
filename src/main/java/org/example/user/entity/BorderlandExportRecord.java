package org.example.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("borderland_export_record")
public class BorderlandExportRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    /**
     * 带出的卡牌code
     */
    private String cardCode;
    
    /**
     * 带出时的签证ID
     */
    private Long visaId;
    
    private OffsetDateTime createdAt;
}
