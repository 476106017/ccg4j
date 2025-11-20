package org.example.card.data.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("card_data")
public class CardDataEntity {
    @TableId(type = IdType.INPUT)
    private String id;
    private String name;
    private String dataJson; // 存储完整的 JSON 配置
    private Boolean enabled;
    private LocalDateTime updatedAt;
}
