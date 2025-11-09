package org.example.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_card_collection")
public class UserCardCollection {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String cardCode;
    private Integer quantity;
}
