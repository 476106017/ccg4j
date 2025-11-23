package org.example.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("community_channel")
public class CommunityChannel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String type; // PUBLIC, PRIVATE
    private Long ownerId; // null for PUBLIC
    private OffsetDateTime createdAt;
    private OffsetDateTime lastActivityAt;

    // New features
    private Boolean isPinned; // Admin置顶
    private Integer level; // 频道等级
    private Long heat; // 热度值
    private Boolean isRestricted; // 是否仅指定用户发言
}
