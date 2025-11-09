package org.example.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("borderland_visa")
public class BorderlandVisa {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    /**
     * 签证状态: ACTIVE-有效, EXPIRED-过期, PUNISHED-惩罚期
     */
    private String status;
    
    /**
     * 剩余天数
     */
    private Integer daysRemaining;
    
    /**
     * 卡组数据，使用逗号分隔的卡牌code
     */
    private String deckData;
    
    /**
     * 惩罚结束时间
     */
    private OffsetDateTime punishmentEndTime;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
