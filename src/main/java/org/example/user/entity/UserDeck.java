package org.example.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("user_deck")
public class UserDeck {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String deckName;
    private String deckData;  // 逗号分隔的卡牌code列表
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
