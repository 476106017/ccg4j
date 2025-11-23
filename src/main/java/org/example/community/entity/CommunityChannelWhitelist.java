package org.example.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("community_channel_whitelist")
public class CommunityChannelWhitelist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long channelId;
    private Long userId;
}
