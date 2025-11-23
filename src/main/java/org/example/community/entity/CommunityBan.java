package org.example.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("community_ban")
public class CommunityBan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long channelId;
    private Long userId;
    private OffsetDateTime createdAt;
    private OffsetDateTime endTime; // Ban end time
}
